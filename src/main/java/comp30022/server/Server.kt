package comp30022.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.twilio.jwt.accesstoken.AccessToken
import com.twilio.jwt.accesstoken.ChatGrant
import com.twilio.jwt.accesstoken.Grant
import com.twilio.jwt.accesstoken.VideoGrant
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

@SpringBootApplication
@RestController
open class Server {
    @RequestMapping(value = ["/twilio", "/twilio/token"], method = [RequestMethod.POST])
    fun dispatchToken(type: String?, identity: String?, extra: String?): String? {
        if (identity === null || identity.isEmpty() || extra === null || extra.isEmpty()) return null

        val grant: Grant = when (type) {
            "chat" -> ChatGrant().setEndpointId("$TWILIO_APP_NAME:$identity:$extra").setPushCredentialSid(
                TWILIO_FIREBASE_PUSH_CREDENTIAL
            ).setServiceSid(TWILIO_CHAT_SERVICE_SID)
            "video" -> VideoGrant().setRoom(extra)
            else -> return null
        }

        return AccessToken.Builder(
            TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET
        ).identity(identity).grant(grant).build().toJwt()
    }

    @RequestMapping(value = ["/twilio/room/create"], method = [RequestMethod.POST])
    fun createRoom() {
    }

    @RequestMapping(value = ["/twilio/room-status"], method = [RequestMethod.POST])
    fun updateRooms() {
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
    runApplication<Server>(*args)
}
