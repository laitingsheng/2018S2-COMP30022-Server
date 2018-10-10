package comp30022.server

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.twilio.Twilio
import comp30022.server.twilio.TWILIO_ACCOUNT_SID
import comp30022.server.twilio.TWILIO_AUTH_TOKEN
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

private lateinit var USERS: CollectionReference
private lateinit var CALLING: CollectionReference

@SpringBootApplication
class Application : SpringBootServletInitializer() {

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

    // this is the credential for using on google cloud
    var credential = GoogleCredentials.getApplicationDefault();

    if (FirebaseApp.getApps().size == 0) FirebaseApp.initializeApp(
        FirebaseOptions.Builder().setCredentials(credential).build()
    )


    FirestoreClient.getFirestore().run {
        USERS = collection("users")
        CALLING = collection("calling")
    }

    Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)
//    runApplication<Server>(*args)
    SpringApplication.run(Server::class.java, *args)
}
