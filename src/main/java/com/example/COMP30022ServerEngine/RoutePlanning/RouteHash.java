package com.example.COMP30022ServerEngine.RoutePlanning;

import com.google.maps.model.LatLng;

import java.util.Arrays;

public class RouteHash {
    public static int hashOriginsDestinations(LatLng[] origins, LatLng[] destinations) {
        //Sort the array first for hashing
        Arrays.sort(origins);
        Arrays.sort(destinations);

        //Create Hashing
        int originsHashCode = Arrays.deepHashCode(origins);
        int desitnationsHashCode = Arrays.deepHashCode(destinations);

        //an additionaly layer of hashing as key value for result
        int odHashCode = Arrays.hashCode(new int[]{originsHashCode, desitnationsHashCode});

        return odHashCode;
    }
}
