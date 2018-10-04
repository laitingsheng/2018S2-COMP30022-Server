package comp30022.server.Firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream
import java.nio.file.Paths

private val firestore = FirestoreClient.getFirestore(
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
    ) else FirebaseApp.getInstance()
)

fun getFirestore(): Firestore {
    return firestore
}
