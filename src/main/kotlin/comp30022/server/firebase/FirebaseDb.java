package comp30022.server.firebase;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import comp30022.server.exception.DbException;
import comp30022.server.grouping.Group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirebaseDb {

    public static final String ROUTEHASHDB = "routeResult";
    public static final String USERLOCATIONDB = "userToLocation";
    public static final String GROUPINFO = "groupInfo";
    private static final Logger LOGGER = Logger.getLogger(FirebaseDb.class.getName());
    private Firestore db;

    /**
     * Get the firebase instance when this class is initialized
     */
    public FirebaseDb() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).build();
            if (FirebaseApp.getApps().size() == 0) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    /**
     * Get the user's location from userToLocation database on CloudFirestore
     * @param userId: user's uuid
     * @return: Hashmap that contains all user's realtime information
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

    /**
     * Given a hashKey, create by the route
     * @param hashKey: int, the hashkey of route
     * @return: the cached JSON response from Google in JSON String
     */
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

    /**
     * cache the result to firebase
     * @param hashKey: the hash key
     * @param routeResult: JSONString response from Google Direction's API
     */
    public void updateRouteResult(int hashKey, String routeResult) {
        //upload to db
        db = FirestoreClient.getFirestore();
        Map<String, Object> docData = new HashMap<>();
        docData.put("route", routeResult);
        ApiFuture<WriteResult> future = db.collection(ROUTEHASHDB).document(Integer.toString(hashKey)).set(docData);
    }

    /**
     * Function to check wheher route result has been cached.
     * If cached, call getRouteResult to return the cache resullt
     * @param hashKey: the hash value of route request for navigation
     * @return: boolen, indicate whether route result has been cached.
     */
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

    /**
     * Get All groups from the firebase
     * @return List of QueryDocumentSnapshot
     */
    public List<QueryDocumentSnapshot> getAllGroups() {
        db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(GROUPINFO).get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents;
        } catch (Exception e) {
            LOGGER.log(Level.INFO, e.toString(), e);
            throw new DbException("Error in getAllGroups");
        }
    }
}
