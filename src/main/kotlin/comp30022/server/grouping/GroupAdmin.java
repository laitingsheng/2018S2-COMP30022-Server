package comp30022.server.grouping;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import comp30022.server.exception.DbException;
import comp30022.server.exception.NoGrouptoJoinException;
import comp30022.server.firebase.FirebaseDb;
import comp30022.server.util.GeoHashing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupAdmin {

    private static final Logger LOGGER = Logger.getLogger(GroupAdmin.class.getName());
    private FirebaseDb db;

    public GroupAdmin() {
        db = new FirebaseDb();
    }

    public List<Map<String, String>> getMembers(String groupId) {
        Firestore db2 = FirestoreClient.getFirestore();
        DocumentReference groupRef = db2.collection(FirebaseDb.GROUPINFO).document(groupId);
        ApiFuture<QuerySnapshot> membersRef = groupRef.collection("members").get();

        List<Map<String, String>> membersInfo = new ArrayList<Map<String, String>>();

        try {
            List<QueryDocumentSnapshot> members = membersRef.get().getDocuments();
            for (DocumentSnapshot member : members) {
                Map<String, String> newMember = new HashMap<String, String>();

                String userId = member.getId();
                GeoPoint userLocatiopn =
                    db2.collection(FirebaseDb.USERLOCATIONDB).document(userId).get().get().getGeoPoint("location");

                newMember.put("id", userId);
                newMember.put(
                    "location",
                    String.format("%f,%f", userLocatiopn.getLatitude(), userLocatiopn.getLongitude())
                );

                membersInfo.add(newMember);
            }
            return membersInfo;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException("Error in getting all members");
        }
    }

    public void quitGroup(String groupId, Map<String, Object> userDocument) {
        Firestore db2 = FirestoreClient.getFirestore();
        DocumentReference groupRef = db2.collection(FirebaseDb.GROUPINFO).document(groupId);
        try {
            ApiFuture<QuerySnapshot> membersRef = groupRef.collection("members").get();
            String memberId = (String)userDocument.get("id");

            // Delete Memeber from the collection
            groupRef.collection("members").document(memberId).delete();

            // Update Group Location

            // delete document
            int membersCount = membersRef.get().getDocuments().size();
            if (membersCount == 0) {
                // case no one left, delete this group
                groupRef.delete();
            } else {
                GeoPoint userLocation = (GeoPoint)userDocument.get("location");
                GeoPoint groupLocation = groupRef.get().get().getGeoPoint("groupLocation");

                GeoPoint newGroupLocation;
                if (membersCount == 1){
                    newGroupLocation = new GeoPoint(
                        groupLocation.getLatitude() * membersCount - userLocation.getLatitude(),
                        groupLocation.getLongitude() * membersCount - userLocation.getLongitude()
                    );
                } else {
                    newGroupLocation = new GeoPoint(
                        (groupLocation.getLatitude() * membersCount - userLocation.getLatitude()) / (membersCount - 1),
                        (groupLocation.getLongitude() * membersCount - userLocation.getLongitude()) / (membersCount - 1)
                    );
                }

                groupRef.update("groupLocation", newGroupLocation);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException("Error in quitting group");
        }
    }

    /**
     * @param userId
     *     id of the user
     * @param userDocument
     *     a document fetched from firebase given userId
     * @param destination
     *     destination for user
     *
     * @return
     *
     * @throws NoGrouptoJoinException
     */
    public String findNearestGroup(String userId, Map<String, Object> userDocument, GeoPoint destination)
        throws NoGrouptoJoinException {
        // Get user's current location and geo hashing
        GeoPoint userLocation = (GeoPoint)userDocument.get("location");

        int precisionLevel = 8; // 8 character to be 200 m3
        String userHash = GeoHashing.hash(userLocation, precisionLevel);

        // get All group's info
        try {
            // Looking for the matched group
            List<QueryDocumentSnapshot> groups = db.getAllGroups();
            for (QueryDocumentSnapshot group : groups) {
                GeoPoint groupLocation = group.getGeoPoint("groupLocation");
                String groupHash = GeoHashing.hash(groupLocation, precisionLevel);
                if (groupHash.equals(userHash)) {
                    String groupId = group.getId();
                    return groupId;
                }
            }

            // If no group, throw exception
            throw new NoGrouptoJoinException();
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException("Error in find NearestGroup");
        }
    }

    /**
     * @param groupId
     *     id of the user
     * @param userDocument
     *     a document fetch from firebase given userId
     * @param destination
     *     destination for user
     */
    public void addUserToGroup(String groupId, Map<String, Object> userDocument, GeoPoint destination) {
        Firestore db2 = FirestoreClient.getFirestore();
        DocumentReference groupRef = db2.collection(FirebaseDb.GROUPINFO).document(groupId);

        try {
            // get total number of users in a group
            ApiFuture<QuerySnapshot> membersRef = groupRef.collection("members").get();
            int membersCount = membersRef.get().getDocuments().size();

            // adding the member
            Map<String, Object> member = createMember(userDocument, destination);
            member.put("role", "member");
            groupRef.collection("members").document((String)userDocument.get("id")).set(member);

            // update groupLocation
            GeoPoint groupLocation = groupRef.get().get().getGeoPoint("groupLocation");
            GeoPoint userLocation = (GeoPoint)userDocument.get("location");
            GeoPoint newGroupLocation = new GeoPoint(
                (groupLocation.getLatitude() * membersCount + userLocation.getLatitude()) / (membersCount + 1),
                (groupLocation.getLongitude() * membersCount + userLocation.getLongitude()) / (membersCount + 1)
            );
            groupRef.update("groupLocation", newGroupLocation);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException("Error in adding user");
        }
    }

    public Map<String, Object> createMember(Map<String, Object> userDoc, GeoPoint destination) {
        Map<String, Object> member = new HashMap<>();
        Instant instant = Instant.now();
        member.put("destination", destination);
        member.put("joinTime", instant.toString());
        member.put("origin", userDoc.get("location"));
        member.put("id", userDoc.get("id"));
        return member;
    }

    /**
     * @param userId:
     *     id of the user
     * @param userDocument:
     *     a document fetch from Firebase about user
     * @param destination:
     *     the destination of user
     *
     * @return
     *
     * @throws DbException
     */
    public String createGroup(String userId, Map<String, Object> userDocument, GeoPoint destination)
        throws DbException {

        // create for field
        Map<String, Object> group = new HashMap<>();
        group.put("groupLocation", userDocument.get("location"));
        group.put("channelId", "null");

        // Create the Group document
        // update to database
        Firestore db2 = FirestoreClient.getFirestore();
        ApiFuture<DocumentReference> addedDocRef = db2.collection(FirebaseDb.GROUPINFO).add(group);

        try {

            String groupUUID = addedDocRef.get().getId();
            // create for document
            Map<String, Object> member = createMember(userDocument, destination);
            member.put("role", "owner");
            db2.collection(FirebaseDb.GROUPINFO).document(groupUUID).collection("members").document(userId).set(member);
            return groupUUID;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new DbException("Error in creating group");
        }
    }
}
