package comp30022.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.twilio.Twilio
import com.twilio.jwt.accesstoken.AccessToken
import com.twilio.jwt.accesstoken.ChatGrant
import com.twilio.jwt.accesstoken.Grant
import com.twilio.jwt.accesstoken.VideoGrant
import com.twilio.rest.notify.v1.service.Binding
import com.twilio.rest.notify.v1.service.Notification
import com.twilio.rest.video.v1.Room
import comp30022.server.twilio.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Level
import java.util.logging.Logger

private val LOGGER: Logger = Logger.getLogger(Server::class.java.name)
private lateinit var USERS: CollectionReference
private lateinit var CALLING: CollectionReference

private fun buildToken(grant: Grant, identity: String): AccessToken {
    return AccessToken.Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET).identity(identity).grant(grant)
        .build()
}

@SpringBootApplication
@RestController
open class Server {
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

    @RequestMapping(value = ["/twilio/chat/token"], method = [RequestMethod.POST])
    fun notifyMembers(): Boolean {
        return false
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
                Notification.creator(TWILIO_SERVICE_SID).setIdentity(identity).setTag("video").setBody(roomSID)
                    .setPriority(
                        Notification.Priority.HIGH
                    ).create()
                true
            }
        } catch (t: Throwable) {
            LOGGER.log(Level.INFO, "Invalid request", t)
            // any exceptions will return false represents invitation fail
            false
        }
    }
}

fun main(args: Array<String>) {
    if (FirebaseApp.getApps().size == 0) FirebaseApp.initializeApp(
        FirebaseOptions.Builder().setCredentials(
            GoogleCredentials.fromStream(
                java.io.FileInputStream(
                    java.nio.file.Paths.get(
                        ".", "src", "main", "resources", "firebase-admin-sdk.json"
                    ).toAbsolutePath().normalize().toString()
                )
            )
            //            GoogleCredentials.getApplicationDefault()
        ).build()
    )

    FirestoreClient.getFirestore().run {
        USERS = collection("users")
        CALLING = collection("calling")
    }

    Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)
    runApplication<Server>(*args)
}
