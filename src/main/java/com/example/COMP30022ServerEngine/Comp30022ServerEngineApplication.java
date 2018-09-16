package com.example.COMP30022ServerEngine;


import com.example.COMP30022ServerEngine.RoutePlanning.RoutePair;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.COMP30022ServerEngine.Constant.GOOGLEMAPAPIKEY;

import com.example.COMP30022ServerEngine.RoutePlanning.RoutePlanner;

import javax.xml.ws.Response;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {

    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());

    //Maps API initialisation
    private GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey(GOOGLEMAPAPIKEY)
            .build();
    //Firebase Admin Initialisation


    //Server start program
    public static void main(String[] args) {
        SpringApplication.run(Comp30022ServerEngineApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {

        return "Guys the server for GUGUGU is now running";
    }


    @RequestMapping(value="/sum", method = RequestMethod.POST)
    public int calculateSum(@RequestParam("num1") int number1, @RequestParam("num2") int number2){
        return number1+number2;
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
    public ResponseEntity grouping(String user_id){
        /*
            get this user's location

            groupID = GroupUsers

            return the groupID
         */
        return ResponseEntity.badRequest().body("Not Yet implemented");
    }

}