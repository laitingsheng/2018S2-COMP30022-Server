package comp30022.server.firebase;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
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

    private static final Logger LOGGER = Logger.getLogger(FirebaseDb.class.getName());
    private static final String ROUTEHASHDB = "routeResult";
    private static final String USERLOCATIONDB = "userToLocation";
    private static final String GROUPINFO = "groupInfo";

    private Firestore db;

    public FirebaseDb() {
        try {
            //Comment this for deploy
//            InputStream serviceAccount = new FileInputStream(Constant.FIREBASEADMINKEYPATH);
//            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

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

    public List<QueryDocumentSnapshot> getAllGroups(){
        ApiFuture<QuerySnapshot> future = db.collection(GROUPINFO).get();
        try{
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents;
        } catch (Exception e){
            LOGGER.log(Level.INFO, e.toString(), e);
            throw new DbException("Error in getAllGroups");
        }
    }

    public void createGroup(Group group){

    }
}
