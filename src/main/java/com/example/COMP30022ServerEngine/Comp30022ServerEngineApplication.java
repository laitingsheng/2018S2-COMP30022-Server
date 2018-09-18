package com.example.COMP30022ServerEngine;


import com.example.COMP30022ServerEngine.RoutePlanning.RoutePair;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.COMP30022ServerEngine.Constant.GOOGLEMAPAPIKEY;
import static com.example.COMP30022ServerEngine.Constant.FIREBASEADMINKEYPATH;

import com.example.COMP30022ServerEngine.RoutePlanning.RoutePlanner;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {

    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());

    //Maps API initialisation
    private static GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey(GOOGLEMAPAPIKEY)
            .build();

    //Server start program
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, FIREBASEADMINKEYPATH);
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
    public DirectionsResult routePlanning(@RequestBody RoutePair pairs) {
        RoutePlanner planner = new RoutePlanner(geoApiContext);
        try {
            DirectionsResult result = planner.getDirections(pairs.origins, pairs.destinations);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            return null;
        }
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