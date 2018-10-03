package com30022.server.RoutePlanning;


import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RoutePlanner {

    private static final Logger LOGGER = Logger.getLogger(RoutePlanner.class.getName());
    private GeoApiContext geoApiContext;

    public RoutePlanner(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public DirectionsResult getDirections(LatLng[] origins, LatLng[] destinations) throws Exception {
        LatLng[] wayPoints = combineWayPoints(origins, destinations);

        DirectionsResult result =
                DirectionsApi.newRequest(geoApiContext)
                        .origin(origins[0])
                        .destination(destinations[0])
                        .waypoints(wayPoints)
                        .optimizeWaypoints(true)
                        .await();
        return result;
    }

    private LatLng[] combineWayPoints(LatLng[] origins, LatLng[] destinations) {
        List<LatLng> wayPoints = new ArrayList<>();
        for (LatLng o : origins) {
            wayPoints.add(o);
        }
        for (LatLng d : destinations) {
            wayPoints.add(d);
        }

        LatLng[] result = new LatLng[wayPoints.size()];
        return wayPoints.toArray(result);
    }
}
