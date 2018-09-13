package com.example.COMP30022ServerEngine.RoutePlanning;


import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {

    private GeoApiContext geoApiContext;
    public RoutePlanner(GeoApiContext geoApiContext){
        this.geoApiContext = geoApiContext;
    }

    public DirectionsRoute[] getDirections (String[] origins, String[] destinations)throws Exception{
        String[] wayPoints = combineWayPoints(origins, destinations);

        DirectionsResult result =
                DirectionsApi.newRequest(geoApiContext)
                .origin(origins[0])
                .destination(destinations[0])
                .waypoints(wayPoints)
                .optimizeWaypoints(true)
                .await();
        return result.routes;
    }

    private String[] combineWayPoints(String[] origins, String[] destinations){
        List<String> wayPoints = new ArrayList<>();
        for(String o: origins){
            wayPoints.add(o);
        }
        for(String d:destinations){
            wayPoints.add(d);
        }

        String[] result = new String[wayPoints.size()];
        return wayPoints.toArray(result);
    }
}
