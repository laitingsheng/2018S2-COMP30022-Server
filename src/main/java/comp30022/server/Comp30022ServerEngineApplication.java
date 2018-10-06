package comp30022.server;

import com.google.cloud.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.ChatGrant;
import com.twilio.jwt.accesstoken.Grant;
import comp30022.server.exception.NoGrouptoJoinException;
import comp30022.server.firebase.FirebaseDb;
import comp30022.server.grouping.GroupAdmin;
import comp30022.server.routeplanning.RouteHash;
import comp30022.server.routeplanning.RoutePlanner;
import comp30022.server.util.Converter;
import comp30022.server.util.GeoHashing;
import comp30022.server.twilio.TokenResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static comp30022.server.twilio.TwilioConstants.*;

@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {

    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());

    //Maps API initialisation
    private static GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build();

    //Firebase DB initialization
    private static FirebaseDb db = new FirebaseDb();

    private static HashMap<String, TokenGenerator> generators = new HashMap<>();

    static {
        generators.put("chat", (uid, device) -> {
            ChatGrant grant = new ChatGrant();
            grant.setServiceSid(TWILIO_CHAT_SERVICE_SID);
            grant.setPushCredentialSid(TWILIO_FIREBASE_PUSH_CREDENTIAL);
            grant.setEndpointId(TWILIO_APP_NAME + ":" + uid + ":" + device);

            return buildToken(grant, uid);
        });
        // TODO: Add video token generator
        // TODO: Add audio token generator
    }

    private static AccessToken buildToken(Grant grant, String uid) {
        return new AccessToken.Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET)
            .identity(uid)
            .grant(grant)
            .build();
    }

    //Server start program
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Application has started running");
        SpringApplication.run(Comp30022ServerEngineApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "Guys the server for GUGUGU is now running at version 10:12";
    }

    /*
       {
        "origins":[A,B,C,D,E]
        "destinations":[A,B,C,D,E]
       }
     */
    @RequestMapping(value = "/route", method = RequestMethod.POST)
    public ResponseEntity routePlanning(@RequestBody Map<String, String[]> pairs) {
        RoutePlanner planner = new RoutePlanner(geoApiContext);
        try {
            LOGGER.log(Level.INFO, pairs.toString());

            // Sort the array first for hashing
            Arrays.sort(pairs.get("origins"));
            Arrays.sort(pairs.get("destinations"));

            // Parse the array
            GeoPoint[] origins = Converter.parseGeoPoints(pairs.get("origins"));
            GeoPoint[] destinations = Converter.parseGeoPoints(pairs.get("destinations"));

            // Get Hashing
            int routeHashKey = RouteHash.hashOriginsDestinations(origins, destinations);

            if (db.routeResultInDb(routeHashKey)) {
                //fetch string from the db
                String routeString = db.getRouteResult(routeHashKey);

                return ResponseEntity.ok(routeString);
            } else {
                //get result
                DirectionsResult result = planner.getDirections(origins, destinations);
                if (result.routes.length == 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No route avaiable");
                }

                //convert to string for storage
                Gson objGson = new GsonBuilder().setPrettyPrinting().create();
                String routeString = objGson.toJson(result);

                //upload string to db
                db.updateRouteResult(routeHashKey, routeString);

                return ResponseEntity.ok(routeString);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping(value = "/group/joingroup", method = RequestMethod.POST)
    public String searchGroupId(String userId, String destination) {
        //Hard code for development
        String testUserId = "testUserUUID";

        Map<String, Object> userDocument = db.getUserLocationInfo(testUserId);
        // hash user's current location and destination location for grouping
        GeoPoint userLocation = (GeoPoint)userDocument.get("location");
        GeoPoint userDestination = (GeoPoint)userDocument.get("destination");
        String neighbourHash = GeoHashing.hash(userLocation, 8);

        GroupAdmin groupControl = new GroupAdmin();
        String group;
        // Go Through All Group too see the matching
        try{
            // Case we can find a group
             group = groupControl.findNearestGroup(userId, destination);
        } catch (NoGrouptoJoinException e){
            // case we cannot find a group
             group = groupControl.createGroup(userId);
        }
        return group;
    }

    @RequestMapping(value = "/grouping", method = RequestMethod.POST)
    public ResponseEntity grouping(String user_id) {
        /*
            get this user's location

            groupID = GroupUsers

            return the groupID
         */
        return ResponseEntity.badRequest().body("Not Yet implemented");
    }

    @RequestMapping(value = "/twilio/token", method = RequestMethod.POST)
    public TokenResponse dispatchToken(String type, String uid, String device) {
        return new TokenResponse(uid, generators.get(type).generate(uid, device));
    }

    @FunctionalInterface
    private interface TokenGenerator {
        AccessToken generate(String uid, String device);
    }
}
