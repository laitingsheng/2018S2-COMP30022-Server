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
import comp30022.server.twilio.*
import comp30022.server.util.Converter
import org.springframework.boot.SpringApplication
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
    return AccessToken.Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET).identity(identity).grant(grant)
        .build()
}

@SpringBootApplication
@RestController
class Server: SpringBootServletInitializer() {

    private val geoApiContext = GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build()
    private val db = FirebaseDb()

    init {
        // this is the credential for using on google cloud
        LOGGER.log(Level.INFO, "before cretential");
        var credential = GoogleCredentials.getApplicationDefault();

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

    /**
     * Merge from Java
     */

    @GetMapping("/hello")
    fun helloKotlin(): String {
        return "hello world"
    }

    @RequestMapping(value = ["/"], method = [RequestMethod.GET])
    fun hello(): String {
        return "Guys the server for GUGUGU is now running at version 10:58"
    }

    /*
       {
        "origins":[A,B,C,D,E]
        "destinations":[A,B,C,D,E]
       }
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

            if (db.routeResultInDb(routeHashKey)) {
                //fetch string from the db
                val routeString = db.getRouteResult(routeHashKey)

                return ResponseEntity.ok<String>(routeString!!)
            } else {
                //get result
                val result = planner.getDirections(origins, destinations)
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

    // Get All Member's location
    @RequestMapping(value = ["/group/getmembers"], method = [RequestMethod.POST])
    fun getMembers(groupId: String): List<Map<String, String>> {
        val groupControl = GroupAdmin()
        try {
            var members = groupControl.getMembers(groupId);
            return members;
        } catch (e: RuntimeException) {
            var members: List<Map<String, String>> = listOf(hashMapOf("error" to "error"))
            return members
        }
    }

    // Delete Group
    @RequestMapping(value = ["/group/quitgroup"], method = [RequestMethod.POST])
    fun quitGroup(userId: String, groupId: String, response: HttpServletResponse): String {
        val groupControl = GroupAdmin()
        val uerDocument = db.getUserLocationInfo(userId)
        try{
            groupControl.quitGroup(groupId, uerDocument)
            return "Success"
        } catch (e: RuntimeException) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            return "Error"
        }
    }

    // For join Group
    @RequestMapping(value = ["/group/joingroup"], method = [RequestMethod.POST])
    fun searchGroupId(userId: String, destination: String, response: HttpServletResponse): String {
        val groupControl = GroupAdmin()
        val dest = Converter.parseGeoPoint(destination)

        val userDocument = db.getUserLocationInfo(userId)

        // Go Through All Group too see the matching
        try {
            // Case we can find a group
            val groupId = groupControl.findNearestGroup(userId, userDocument, dest)
            groupControl.addUserToGroup(groupId, userDocument, dest)
            return groupId
        } catch (e: NoGrouptoJoinException) {
            // case we cannot find a group
            return groupControl.createGroup(userId, userDocument, dest)
        } catch (e: RuntimeException) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            return "Error"
        }
    }

    // For Creating group
    @RequestMapping(value=["/group/creategroup"], method = [RequestMethod.POST])
    fun createGroup(userId: String, destination: String, response: HttpServletResponse): String{
        val groupControl = GroupAdmin()
        val dest = Converter.parseGeoPoint(destination)
        val userDocument = db.getUserLocationInfo(userId)

        try {
            return groupControl.createGroup(userId, userDocument, dest)
        } catch (e: RuntimeException) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            return "Error"
        }
    }

    @RequestMapping(value = ["/twilio/channel/create"], method = [RequestMethod.POST])
    fun createChannel(identity: String?): String?
    {
        return try {
            Channel.creator(TWILIO_SERVICE_SID).setCreatedBy(identity).create().sid
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "channel creation fail", t)
            null
        }
    }

    @RequestMapping(value = ["/twilio/channel/retrieve"], method = [RequestMethod.POST])
    fun retrieveChannel(channelSid: String?): Channel?
    {
        return try {
            Channel.fetcher(TWILIO_SERVICE_SID, channelSid).fetch();
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "channel fetching fail", t)
            null
        }
    }

    @RequestMapping(value = ["/twilio/member/join"], method = [RequestMethod.POST])
    fun memberJoin(channelSID: String?, identity: String?): String?
    {
        return try {
            Member.creator(TWILIO_SERVICE_SID, channelSID!!, identity!!).create().sid
        } catch (t: Throwable) {
            LOGGER.log(Level.SEVERE, "member creation fail", t)
            null
        }
    }
}

fun main(args: Array<String>) {
    // this is the credentigclal to use on local
    //    var credential = GoogleCredentials.fromStream(
    //        FileInputStream(
    //            Paths.get(
    //                ".", "src", "main", "resources", "firebase-admin-sdk.json"
    //            ).toAbsolutePath().normalize().toString()
    //        )
    //    )

    runApplication<Server>(*args)
}
