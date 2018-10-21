package comp30022.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.gson.GsonBuilder
import com.google.maps.GeoApiContext
import com.twilio.Twilio
import com.twilio.jwt.accesstoken.AccessToken
import com.twilio.jwt.accesstoken.ChatGrant
import com.twilio.jwt.accesstoken.Grant
import com.twilio.jwt.accesstoken.VideoGrant
import com.twilio.rest.chat.v1.service.channel.Member
import com.twilio.rest.chat.v2.service.Channel
import com.twilio.rest.notify.v1.service.Binding
import com.twilio.rest.notify.v1.service.Notification
import com.twilio.rest.video.v1.Room
import comp30022.server.exception.NoGrouptoJoinException
import comp30022.server.firebase.FirebaseDb
import comp30022.server.grouping.GroupAdmin
import comp30022.server.routeplanning.RouteHash
import comp30022.server.routeplanning.RoutePlanner
import comp30022.server.util.Converter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Arrays
import java.util.logging.Level
import java.util.logging.Logger
import javax.servlet.http.HttpServletResponse

private val LOGGER: Logger = Logger.getLogger(Server::class.java.name)
private lateinit var USERS: CollectionReference
private lateinit var CALLING: CollectionReference

private fun buildToken(grant: Grant, identity: String): AccessToken {
    return AccessToken.Builder(
        TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET
    ).identity(identity).grant(grant).build()
}

@SpringBootApplication
@RestController
class Server : SpringBootServletInitializer() {

    private val geoApiContext = GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build()
    private val db = FirebaseDb()

    init {
        // this is the credential for using on google cloud
        LOGGER.log(Level.INFO, "before cretential");
        var credential = GoogleCredentials.getApplicationDefault();

        // initialise the firebase db
        LOGGER.log(Level.INFO, "before firestore initialisation");
        if (FirebaseApp.getApps().size == 0) FirebaseApp.initializeApp(
            FirebaseOptions.Builder().setCredentials(credential).build()
        )


        LOGGER.log(Level.INFO, "get users and calling");
        FirestoreClient.getFirestore().run {
            USERS = collection("users")
            CALLING = collection("calling")
        }

        LOGGER.log(Level.INFO, "twilio init");
        Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)
        LOGGER.log(Level.INFO, "init finish");
    }

    /**
     * End Point for binding a user to twillo server
     * @return twillo binding sid
     */
    @RequestMapping(value = ["/twilio/register"], method = [RequestMethod.POST])
    fun register(identity: String?, address: String?, tag: String?): String? {
        return try {
            if (identity!!.isEmpty() || address!!.isEmpty() || tag!!.isEmpty()) null else Binding.creator(
                TWILIO_SERVICE_SID, identity, Binding.BindingType.FCM, address
            ).setCredentialSid(TWILIO_FIREBASE_PUSH_CREDENTIAL).setTag(tag).create().sid
        } catch (t: Throwable) {
            LOGGER.log(Level.INFO, "Invalid request", t)
            // any exceptions will return false represents invitation fail
            null
        }
    }

    /**
     * End Point for revoke the binding of a twillo user from the twillo server
     * @return boolean indicate success of not for revoking binding
     */
    @RequestMapping(value = ["/twilio/deregister"], method = [RequestMethod.POST])
    fun deregister(sid: String?): Boolean {
        return try {
            Binding.deleter(TWILIO_SERVICE_SID, sid!!).delete()
        } catch (e: Exception) {
            LOGGER.log(Level.INFO, "Invalid request", e)
            // any exceptions will return false represents invitation fail
            false
        }
    }

    /**
     * Get the twillo token that will be used to create chat channel
     * @param identity: user's uuid
     * @param extra: firebase instance id for each android device
     * @return the token that will be used to create channel
     */
    @RequestMapping(value = ["/twilio/chat/token"], method = [RequestMethod.GET, RequestMethod.POST])
    fun dispatchChatToken(identity: String?, extra: String?): String? {
        return try {
            // identity and extra should be non-null and non-empty
            if (identity!!.isEmpty() || extra!!.isEmpty()) null
            else buildToken(
                ChatGrant().setEndpointId("$TWILIO_APP_NAME:$identity:$extra").setPushCredentialSid(
                    TWILIO_FIREBASE_PUSH_CREDENTIAL
                ).setServiceSid(TWILIO_CHAT_SERVICE_SID), identity
            ).toJwt()
        } catch (t: Throwable) {
            LOGGER.log(Level.INFO, "invalid token request", t)
            // any exceptions will incur a response of null
            null
        }
    }

    /**
     *Endpoint that notify all uses in a group
     * @return notify succesful or not
     */
    @RequestMapping(value = ["/twilio/chat/notify"], method = [RequestMethod.POST])
    fun notifyMembers(guid: String?): Boolean {
        return try {
            if (guid!!.isEmpty()) false else {
                // notify all members in a group for a new message
                Notification.creator(TWILIO_SERVICE_SID).setTag(listOf("default", guid))
                    .setPriority(Notification.Priority.LOW).create()
                true
            }
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "invalid notification request", t)
            false
        }
    }

    /**
     * Given a type of room (PEER_TO_PEER / GROUP), create a channel for this room
     * @return channel's uuid that represent the room
     */
    @RequestMapping(value = ["/twilio/room/create"], method = [RequestMethod.POST])
    fun createRoom(type: String?): String? {
        return try {
            // convert the type to all upper case letters since all values in Room.RoomType are in upper case
            Room.RoomType.valueOf(type!!.toUpperCase()).let {
                // create a room of the given type which should be non-null and be in the Room.RoomType enum
                Room.creator().setType(it).setEnableTurn(false).create().sid
            }
        } catch (t: Throwable) {
            LOGGER.log(Level.INFO, "invalid room creation request", t)
            // any exceptions will incur a response of null
            null
        }
    }

    /**
     * Given a channel's uuid that represent the room, delete this channel
     * @return Boolean: represent success or not
     */
    @RequestMapping(value = ["/twilio/room/delete"], method = [RequestMethod.POST])
    fun deleteRoom(sid: String?): Boolean {
        return try {
            // mark the room as COMPLETED, a non-exist room will have a status of FAIL
            Room.updater(sid!!, Room.RoomStatus.COMPLETED).update().status == Room.RoomStatus.COMPLETED
        } catch (t: Throwable) {
            LOGGER.log(Level.INFO, "invalid room deletion request", t)
            // the deletion was not completed
            false
        }
    }

    /**
     * End point for getting toke for call, given user's identity
     * @param identity: user's uuid
     * @param extra: firebase instance id for each android device
     * @return the token that will be used to create channel
     */
    @RequestMapping(value = ["/twilio/call/token"], method = [RequestMethod.GET, RequestMethod.POST])
    fun dispatchToken(identity: String?, extra: String?): String? {
        return try {
            // identity and extra should be non-null and non-empty
            if (identity!!.isEmpty() || extra!!.isEmpty()) null
            else buildToken(VideoGrant().setRoom(extra), identity).toJwt()
        } catch (t: Throwable) {
            LOGGER.log(Level.INFO, "invalid token request", t)
            // any exceptions will incur a response of null
            null
        }
    }

    /**
     * End point for inviting the user through notification
     * @param identity: user's uusid
     * @param roomSID: channel id that represent twillo room, which user will join
     * @return boolean, represent success or not
     */
    @RequestMapping(value = ["twilio/call/invite"], method = [RequestMethod.POST])
    fun invite(identity: String?, roomSID: String?): Boolean {
        return try {
            if (identity!!.isEmpty() || roomSID!!.isEmpty()) false
            else {
                Notification.creator(TWILIO_SERVICE_SID).setIdentity(identity).setTag(listOf("default", "video"))
                    .setBody(roomSID).setPriority(Notification.Priority.HIGH).create()
                true
            }
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "invalid invitation notification", t)
            false
        }
    }

    @RequestMapping(value = ["/twilio/channel/create"], method = [RequestMethod.POST])
    fun createChannel(identity: String?): String? {
        return try {
            Channel.creator(TWILIO_SERVICE_SID).setCreatedBy(identity).create().sid
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "channel creation fail", t)
            null
        }
    }

    /**
     * End point for user to find the channel ID  given channel's SId
     */
    @RequestMapping(value = ["/twilio/channel/retrieve"], method = [RequestMethod.POST])
    fun retrieveChannel(channelSid: String?): Channel? {
        return try {
            Channel.fetcher(TWILIO_SERVICE_SID, channelSid!!).fetch()
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "channel fetching fail", t)
            null
        }
    }

    /**
     * End point for user to join channel (Chat or VideoChat)
     * @param channelSID: id of Chat channel that will join
     * @param identity: user's identity
     */
    @RequestMapping(value = ["/twilio/member/join"], method = [RequestMethod.POST])
    fun memberJoin(channelSID: String?, identity: String?): String? {
        return try {
            Member.creator(TWILIO_SERVICE_SID, channelSID!!, identity!!).create().sid
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "member creation fail", t)
            null
        }
    }

    /**
     * Merge from Java
     */

    /**
     * Test end point to make sure deployment success
     */
    @GetMapping("/hello")
    fun helloKotlin(): String {
        return "hello world"
    }

    /**
     * Test end point to make sure deployment success
     */
    @RequestMapping(value = ["/"], method = [RequestMethod.GET])
    fun hello(): String {
        return "Guys the server for GUGUGU is now running at version 10:58"
    }

    /**
     * Endpoint for navigation the toure
     * {
     * "origins":[A,B,C,D,E]
     * "destinations":[A,B,C,D,E]
     * }
     * @return JSON string, see https://developers.google.com/maps/documentation/directions/start for sample response
     */
    @RequestMapping(value = ["/route"], method = [RequestMethod.POST])
    fun routePlanning(@RequestBody pairs: Map<String, Array<String>>): ResponseEntity<*> {
        val planner = RoutePlanner(geoApiContext)
        try {
            LOGGER.log(Level.INFO, pairs.toString())

            // Sort the array first for hashing
            Arrays.sort(pairs["origins"])
            Arrays.sort(pairs["destinations"])

            // Parse the array
            val origins = Converter.parseGeoPoints(pairs["origins"])
            val destinations = Converter.parseGeoPoints(pairs["destinations"])

            // Get Hashing
            val routeHashKey = RouteHash.hashOriginsDestinations(origins, destinations)

            // if result is cached, return the result
            if (db.routeResultInDb(routeHashKey)) {
                //fetch string from the db
                val routeString = db.getRouteResult(routeHashKey)

                return ResponseEntity.ok<String>(routeString!!)
            } else {
                //get result from directions'API
                val result = planner.getDirections(origins, destinations)

                //if google cloud returns no result
                if (result.routes.size == 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No route avaiable")
                }

                //convert to string for storage
                val objGson = GsonBuilder().setPrettyPrinting().create()
                val routeString = objGson.toJson(result)

                //upload string to db
                db.updateRouteResult(routeHashKey, routeString)

                return ResponseEntity.ok(routeString)
            }
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, e.toString(), e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body<Any>(null)
        }
    }

    /**
     * End point for getting all user's location given a groupID
     * @return list of JSON dictionary
     */
    // Get All Member's location
    @RequestMapping(value = ["/group/getmembers"], method = [RequestMethod.POST])
    fun getMembers(groupId: String): List<Map<String, String>> {
        val groupControl = GroupAdmin()
        return try {
            val members = groupControl.getMembers(groupId)
            members
        } catch (e: Exception) {
            var members: List<Map<String, String>> = listOf(hashMapOf("error" to "error"))
            members
        }
    }

    /**
     * End point for delete a user from group given groupID
     * @param userId: user's id
     * @param groupId: group id
     * @return sring represent success or not
     */
    // Delete Group
    @RequestMapping(value = ["/group/quitgroup"], method = [RequestMethod.POST])
    fun quitGroup(userId: String, groupId: String, response: HttpServletResponse): String {
        val groupControl = GroupAdmin()
        val uerDocument = db.getUserLocationInfo(userId)
        try {
            groupControl.quitGroup(groupId, uerDocument)
            return "Success"
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            return "Error"
        }
    }

    /**
     * Given a userID and destination, peroform our grouping algorithm to put user into group
     * @param: userId: user's id
     * @param: destination: user coordinat string in "lat,long" form
     * @return String, represent group it.
     */
    // For join Group
    @RequestMapping(value = ["/group/joingroup"], method = [RequestMethod.POST])
    fun searchGroupId(userId: String, destination: String, response: HttpServletResponse): String {
        val groupControl = GroupAdmin()
        val dest = Converter.parseGeoPoint(destination)

        val userDocument = db.getUserLocationInfo(userId)

        // Go Through All Group too see the matching
        return try {
            // Case we can find a group
            val groupId = groupControl.findNearestGroup(userId, userDocument, dest)
            groupControl.addUserToGroup(groupId, userDocument, dest)
            groupId
        } catch (e: NoGrouptoJoinException) {
            // case we cannot find a group
            groupControl.createGroup(userId, userDocument, dest)
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            "Error"
        }
    }

    /**
     * endpoint for user to create a group for our grouping feature
     * @param userId: user's uuid
     * @param: destination: user coordinat string in "lat,long" form
     * @return String, represent group it.
     */
    // For Creating group
    @RequestMapping(value = ["/group/creategroup"], method = [RequestMethod.POST])
    fun createGroup(userId: String, destination: String, response: HttpServletResponse): String {
        val groupControl = GroupAdmin()
        val dest = Converter.parseGeoPoint(destination)
        val userDocument = db.getUserLocationInfo(userId)

        try {
            return groupControl.createGroup(userId, userDocument, dest)
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            return "Error"
        }
    }


}

fun main(args: Array<String>) {
    runApplication<Server>(*args)
}

