package comp30022.server.Firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirebaseDb {

    private static final Logger LOGGER = Logger.getLogger(FirebaseDb.class.getName());
    private static final String ROUTEHASHDB = "routeResult";
    private static final String USERLOCATIONDB = "userToLocation";

    private Firestore db;

    public void updateUser(String userId) {
        /*
        1. get old user location from user-location database
        2. delete user from location-user database
        3. update new user location to userlocation database
        4. update new user location to location-user database
         */
    }
    /*
    function relates to route result
     */

    public Map<String, Object> getUserLocationInfo(String userId) {
        db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(USERLOCATIONDB).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.getData();
            }
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            return null;
        }
    }

    public String getRouteResult(int hashKey) {
        db = FirestoreClient.getFirestore();

        DocumentReference docRef = db.collection(ROUTEHASHDB).document(Integer.toString(hashKey));
        ApiFuture<DocumentSnapshot> future = docRef.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                //get route result as string
                String routeString = document.getData().get("route").toString();

                return routeString;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            return null;
        }
    }

    public void updateRouteResult(int hashKey, String routeResult) {

        //upload to db
        db = FirestoreClient.getFirestore();
        Map<String, Object> docData = new HashMap<>();
        docData.put("route", routeResult);
        ApiFuture<WriteResult> future = db.collection(ROUTEHASHDB).document(Integer.toString(hashKey)).set(docData);
    }

    public boolean routeResultInDb(int hashKey) {
        db = FirestoreClient.getFirestore();

        DocumentReference docRef = db.collection(ROUTEHASHDB).document(Integer.toString(hashKey));

        ApiFuture<DocumentSnapshot> future = docRef.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            return false;
        }
    }
}
