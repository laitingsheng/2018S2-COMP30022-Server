package comp30022.server.grouping;

import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import comp30022.server.exception.NoGrouptoJoinException;
import comp30022.server.firebase.FirebaseDb;
import comp30022.server.util.GeoHashing;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupAdmin {

    private static final Logger LOGGER = Logger.getLogger(GroupAdmin.class.getName());
    private FirebaseDb db;

    public GroupAdmin(){
        db = new FirebaseDb();
    }

    public String findNearestGroup(String userId, String destination) throws NoGrouptoJoinException {
        String testUserId = "testUserUUID";

        // Get user's current location and geo hashing
        Map<String, Object> userDocument = db.getUserLocationInfo(testUserId);
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
                    return group.getId();
                }
            }

            // If no group, throw exception
            throw new NoGrouptoJoinException();
        } catch(Exception e){
            LOGGER.log(Level.WARNING, e.toString(), e);
            throw new RuntimeException("Error in find NearestGroup");
        }


    }

    public String createGroup(String userId){
        return null;
    }
}
