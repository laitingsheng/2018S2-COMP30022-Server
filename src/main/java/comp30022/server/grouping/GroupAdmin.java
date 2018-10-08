package comp30022.server.grouping;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import comp30022.server.exception.DbException;
import comp30022.server.exception.NoGrouptoJoinException;
import comp30022.server.firebase.FirebaseDb;
import comp30022.server.util.GeoHashing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupAdmin {

    private static final Logger LOGGER = Logger.getLogger(GroupAdmin.class.getName());
    private FirebaseDb db;

    public GroupAdmin(){
        db = new FirebaseDb();
    }

    /**
     *
     * @param userId id of the user
     * @param userDocument a document fetched from firebase given userId
     * @param destination destination for user
     * @return
     * @throws NoGrouptoJoinException
     */
    public String findNearestGroup(String userId, Map<String, Object> userDocument, GeoPoint destination) throws NoGrouptoJoinException {
        String testUserId = "testUserUUID";

        // Get user's current location and geo hashing
        GeoPoint userLocation = (GeoPoint)userDocument.get("location");

        int precisionLevel = 8; // 8 character to be 200 m3
        String userHash = GeoHashing.hash(userLocation, precisionLevel);

        // get All group's info
        try{
            // Looking for the matched group
            List<QueryDocumentSnapshot> groups = db.getAllGroups();
            for (QueryDocumentSnapshot group: groups){
                GeoPoint groupLocation = group.getGeoPoint("groupLocation");
                String groupHash = GeoHashing.hash(groupLocation, precisionLevel);
                if (groupHash.equals(userHash)) {
                    String groupId = group.getId();

                    addUserToGroup(groupId, userDocument, destination);

                    return groupId;
                }
            }

            // If no group, throw exception
            throw new NoGrouptoJoinException();
        } catch(Exception e){
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException("Error in find NearestGroup");
        }
    }

    /**
     *
     * @param groupId id of the user
     * @param userDocument a document fetch from firebase given userId
     * @param destination destination for user
     */
    public void addUserToGroup(String groupId, Map<String, Object> userDocument, GeoPoint destination){
        Firestore db2 = FirestoreClient.getFirestore();
        DocumentReference group = db2.collection(FirebaseDb.GROUPINFO).document(groupId);
        group.update("members", FieldValue.arrayUnion((String)userDocument.get("id")));
        group.update("origins", FieldValue.arrayUnion((GeoPoint)userDocument.get("location")));

        // TODO Group location is expected to be updated by client after get groupId.
    }

    /**
     *
     * @param userId: id of the user
     * @param userDocument: a document fetch from Firebase about user
     * @param destination: the destination of user
     * @return
     * @throws DbException
     */
    public String createGroup(String userId, Map<String, Object> userDocument, GeoPoint destination) throws
        DbException{
        Map<String, Object> group = new HashMap<>();
        group.put("groupLocation", userDocument.get("loication"));
        String[] members = {(String)userDocument.get("id")};
        group.put("members", members);
        GeoPoint[] origins = {(GeoPoint)userDocument.get("location")};
        group.put("origins", origins);
        GeoPoint[] destinations = {destination};
        group.put("destinations", destinations);

        // update to database
        Firestore db2 = FirestoreClient.getFirestore();
        ApiFuture<DocumentReference> addedDocRef = db2.collection(FirebaseDb.GROUPINFO).add(group);

        try{
            return addedDocRef.get().getId();
        } catch (Exception e){
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new DbException("Error in creating group");
        }
    }
}
