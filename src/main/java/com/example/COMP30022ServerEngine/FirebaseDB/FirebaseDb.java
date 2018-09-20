package com.example.COMP30022ServerEngine.FirebaseDB;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.maps.model.DirectionsResult;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.COMP30022ServerEngine.Constant.FIREBASEADMINKEYPATH;

public class FirebaseDb {

    private static final Logger LOGGER = Logger.getLogger(FirebaseDb.class.getName());
    private static final String ROUTEHASHDB = "routeResult";

    private Firestore db;

    public FirebaseDb() {
        try {
            //Comment this for deploy
            InputStream serviceAccount = new FileInputStream(FIREBASEADMINKEYPATH);
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            //GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    public void updateRouteHashResult(int hashKey, DirectionsResult result) {
        db = FirestoreClient.getFirestore();
        Map<String, Object> docData = new HashMap<>();
        docData.put("geocodedWaypoints", result.geocodedWaypoints);
        docData.put("routes", result.routes);
        ApiFuture<WriteResult> future = db.collection(ROUTEHASHDB).document(Integer.toString(hashKey)).set(docData);
    }


    public void updateUser(String userId) {
        /*
        1. get old user location from user-location database
        2. delete user from location-user database
        3. update new user location to userlocation database
        4. update new user location to location-user database
         */
    }
}
