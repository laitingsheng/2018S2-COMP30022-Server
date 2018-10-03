package comp30022.server;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.ChatGrant;
import com.twilio.jwt.accesstoken.Grant;
import com.twilio.jwt.accesstoken.VoiceGrant;
import comp30022.server.Firebase.FirebaseDb;
import comp30022.server.RoutePlanning.RouteHash;
import comp30022.server.RoutePlanning.RoutePair;
import comp30022.server.RoutePlanning.RoutePlanner;
import comp30022.server.util.GeoHashing;
import comp30022.server.twilio.TokenResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static comp30022.server.Firebase.FirebaseKt.getFirestore;
import static comp30022.server.twilio.Constants.*;

@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {
    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());
    private static final Map<String, TokenGenerator> GENERATORS = new HashMap<>();
    private static final CollectionReference USERS = getFirestore().collection("users");
    //Maps API initialisation
    private static GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build();
    private static FirebaseDb db = new FirebaseDb();

    static {
        GENERATORS.put("chat", (uid, device) -> {
            ChatGrant grant = new ChatGrant();
            grant.setEndpointId(TWILIO_APP_NAME + ":" + uid + ":" + device);
            grant.setPushCredentialSid(TWILIO_FIREBASE_PUSH_CREDENTIAL);
            grant.setServiceSid(TWILIO_CHAT_SERVICE_SID);

            return buildToken(grant, uid);
        });
        // TODO: Add video token generator

        GENERATORS.put("voice", (uid, device) -> {
            VoiceGrant grant = new VoiceGrant();
            grant.setEndpointId(TWILIO_APP_NAME + ":" + uid + ":" + device);
            grant.setIncomingAllow(true);
            grant.setOutgoingApplicationSid(TWILIO_VOICE_APP_SID);
            grant.setPushCredentialSid(TWILIO_FIREBASE_PUSH_CREDENTIAL);

            return buildToken(grant, uid);
        });
    }

    private static AccessToken buildToken(Grant grant, String uid) {
        return new AccessToken.Builder(TWILIO_ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET)
            .identity(uid)
            .grant(grant)
            .build();
    }

    //Server start program
    public static void main(String[] args) {
        //        LOGGER.log(Level.INFO, FIREBASEADMINKEYPATH);
        SpringApplication.run(Comp30022ServerEngineApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "Guys the server for GUGUGU is now running";
    }

    /*
       {
        "origins":[A,B,C,D,E]
        "destinations":[A,B,C,D,E]
       }
     */
    @RequestMapping(value = "/route", method = RequestMethod.POST)
    public ResponseEntity routePlanning(@RequestBody RoutePair pairs) {
        RoutePlanner planner = new RoutePlanner(geoApiContext);
        JsonParser parser = new JsonParser();
        try {
            int routeHashKey = RouteHash.hashOriginsDestinations(pairs.origins, pairs.destinations);

            if (db.routeResultInDb(routeHashKey)) {
                //fetch string from the db
                String routeString = db.getRouteResult(routeHashKey);

                return ResponseEntity.ok(routeString);
            } else {
                //get result
                DirectionsResult result = planner.getDirections(pairs.origins, pairs.destinations);
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
        //hash user's current location and destination location for grouping
        GeoPoint userLocation = (GeoPoint)userDocument.get("location");
        GeoPoint userDestination = (GeoPoint)userDocument.get("destination");
        String neighbourHash = GeoHashing.hash(userLocation, 8);
        String destinationHash = GeoHashing.hash(userDestination, 8);
        return neighbourHash + "+" + destinationHash;

        /*
        1->2->3->4.......->12 level
        each layer has 12 characters
        leaf node:
            userId
            Geopoint
         */

        /*
        after 8th layer, retrieve every user under itb
         */

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

    @RequestMapping(value = {"/twilio", "/twilio/token"}, method = RequestMethod.POST)
    public TokenResponse dispatchToken(String type, String uid, String device) {
        return new TokenResponse(uid, GENERATORS.get(type).generate(uid, device).toJwt());
    }

    @RequestMapping(value = "/twilio/call", method = RequestMethod.POST)
    public boolean createCall(String uid1, String uid2) {
        // TODO: Add user check and create room and voice grant if both free
        return false;
    }

    @FunctionalInterface
    private interface TokenGenerator {
        AccessToken generate(String uid, String device);
    }
}
