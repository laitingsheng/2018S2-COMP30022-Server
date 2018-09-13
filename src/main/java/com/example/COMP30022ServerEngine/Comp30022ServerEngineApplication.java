package com.example.COMP30022ServerEngine;


import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.COMP30022ServerEngine.Constant.GOOGLEMAPAPIKEY;

import com.example.COMP30022ServerEngine.RoutePlanning.RoutePlanner;

import java.util.logging.Level;
import java.util.logging.Logger;


@SpringBootApplication
@RestController
public class Comp30022ServerEngineApplication {

    private static final Logger LOGGER = Logger.getLogger(Comp30022ServerEngineApplication.class.getName());

    private GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey(GOOGLEMAPAPIKEY)
            .build();

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
        "origins":[A,B,C,D,E]
        "destinations":[A,B,C,D,E]
     */
    @RequestMapping(value = "/route", method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    public ResponseEntity routePlanning(String[] origins, String[] destinations) {
        LOGGER.log(Level.INFO, "hitting POST");

        RoutePlanner planner = new RoutePlanner(geoApiContext);
        try {
            DirectionsRoute[] route = planner.getDirections(origins, destinations);
            return ResponseEntity.ok(route.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Origins and Destinations Invalid");
        }
    }

}