package comp30022.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.GeoPoint;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import comp30022.server.exception.NoGrouptoJoinException;
import comp30022.server.grouping.GroupAdmin;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroupAdminTest {

    GeoPoint UNIMELB = new GeoPoint(-37.7964, 144.9612);
    GeoPoint RMIT = new GeoPoint(-37.8077, 144.9632);
    GeoPoint LIB = new GeoPoint(-37.8098, 144.9652);
    GeoPoint NGV = new GeoPoint(-37.8226, 144.9689);
    GeoPoint FLINDER = new GeoPoint(-37.8183, 144.9671);

    @Test
    public void createGroupTest(){
        String userId = "testUserUUID";

        GroupAdmin control = new GroupAdmin();
        Map<String, Object> user = new HashMap<>();
        user.put("location", UNIMELB);
        user.put("id", userId);

        String group = null;
        try{
            group = control.createGroup(userId, user, RMIT);
        } catch (Exception e){
            ;
        };
        assertNotNull(group);
    }

    @Test
    public void addUserToGroupTest(){
        String userId = "testUserUUID2";

        GroupAdmin control = new GroupAdmin();
        Map<String, Object> user = new HashMap<>();
        user.put("location", UNIMELB);
        user.put("id", userId);
        String groupId = "Pg4NoHnCfH7KHpDUJ2Pg";
        control.addUserToGroup(groupId, user, RMIT);

    }

    @Test
    public void findNearestGroupTest(){
        String userId = "testUserUUID3";

        GroupAdmin control = new GroupAdmin();
        Map<String, Object> user = new HashMap<>();
        user.put("location", UNIMELB);

        String group = null;
        try{
            group = control.findNearestGroup(userId, user, RMIT);
            assertNotNull(group);
        } catch (NoGrouptoJoinException ne){
            ;
        } catch (Exception e){
            assertNotNull(group);
        }

    }
}
