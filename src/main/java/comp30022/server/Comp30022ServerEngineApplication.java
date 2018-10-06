package comp30022.server;

import com.google.cloud.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import comp30022.server.Firebase.FirebaseDb;
import comp30022.server.RoutePlanning.RouteHash;
import comp30022.server.RoutePlanning.RoutePlanner;
import comp30022.server.Util.Converter;
import comp30022.server.Util.GeoHashing;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {
    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());
    //Maps API initialisation
    private static GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build();
    private static FirebaseDb db = new FirebaseDb();

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
}
