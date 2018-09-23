package com.example.COMP30022ServerEngine;


import com.example.COMP30022ServerEngine.FirebaseDB.FirebaseDb;
import com.example.COMP30022ServerEngine.RoutePlanning.RouteHash;
import com.example.COMP30022ServerEngine.RoutePlanning.RoutePair;
import com.example.COMP30022ServerEngine.RoutePlanning.RoutePlanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.COMP30022ServerEngine.Constant.GOOGLEMAPAPIKEY;


@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {

    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());

    //Maps API initialisation
    private static GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey(GOOGLEMAPAPIKEY)
            .build();

    //Firebase DB initialization
    private static FirebaseDb db = new FirebaseDb();


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

        return "Not yet implemented";
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