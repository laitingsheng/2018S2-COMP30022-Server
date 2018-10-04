package comp30022.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.twilio.Twilio
import com.twilio.jwt.accesstoken.AccessToken
import com.twilio.jwt.accesstoken.ChatGrant
import com.twilio.jwt.accesstoken.VideoGrant
import com.twilio.rest.video.v1.Room
import comp30022.server.twilio.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.logging.Logger

private val LOGGER: Logger = Logger.getLogger(Server::class.java.name)
private lateinit var ROOMS: CollectionReference

@SpringBootApplication
@RestController
open class Server {
    @RequestMapping(value = ["/twilio", "/twilio/token"], method = [RequestMethod.GET, RequestMethod.POST])
    fun dispatchToken(type: String?, identity: String?, extra: String?): String? {
        return try {
            // identity and extra should be non-null and non-empty
            if (identity!!.isEmpty() || extra!!.isEmpty()) null else when (type) {
                // generate chat grant
                "chat" -> ChatGrant().setEndpointId("$TWILIO_APP_NAME:$identity:$extra").setPushCredentialSid(
                    TWILIO_FIREBASE_PUSH_CREDENTIAL
                ).setServiceSid(TWILIO_CHAT_SERVICE_SID)
                // generate video grant
                "video" -> VideoGrant().setRoom(extra)
                // null if type is not recognised
                else -> null
            }!!.let {
                // build the access token with the given non-null grant as well as the identity and convert it to JWT
                // as the response
                AccessToken.Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET).identity(identity).grant(it)
                    .build().toJwt()
            }
        } catch (e: Exception) {
            // any exceptions will incur a response of null
            null
        }
    }

    @RequestMapping(value = ["/twilio/room/create"], method = [RequestMethod.POST])
    fun createRoom(type: String?): String? {
        return try {
            // convert the type to all upper case letters since all values in Room.RoomType are in upper case
            when(type!!.toUpperCase()) {
                // cast the type to enum type directly if exists
                in Room.RoomType.values().map { it.toString() } -> Room.RoomType.valueOf(type)
                // allow aliases of the enum values
                "G" -> Room.RoomType.GROUP
                "GS" -> Room.RoomType.GROUP_SMALL
                "P2P" -> Room.RoomType.PEER_TO_PEER
                // null if type is neither in the enum nor an alias
                else -> null
            }!!.let {
                // create a room of the given type which should be non-null and be in the Room.RoomType enum
                Room.creator().setType(it).create().run {
                    // record the necessary information in Cloud Firestore for the client
                    ROOMS.document(sid).set(RoomRecord(sid, it.toString(), mediaRegion, maxParticipants))
                    // response the SID of the room to the request
                    sid
                }
            }
        } catch (e: Exception) {
            // any exceptions will incur a response of null
            null
        }
    }

    @RequestMapping(value = ["/twilio/room-status"], method = [RequestMethod.POST])
    fun monitorRooms() {
    }
}

fun main(args: Array<String>) {
    if (FirebaseApp.getApps().size == 0) FirebaseApp.initializeApp(
        FirebaseOptions.builder().setCredentials(
            GoogleCredentials.fromStream(
                FileInputStream(
                    Paths.get(
                        ".", "src", "main", "resources", "firebase-admin-sdk.json"
                    ).toAbsolutePath().normalize().toString()
                )
            )
        ).build()
    )
    ROOMS = FirestoreClient.getFirestore().collection("rooms")

    Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)
    runApplication<Server>(*args)
}
