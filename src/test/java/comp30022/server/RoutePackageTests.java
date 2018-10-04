package comp30022.server;

import apple.laf.JRSUIConstants.Direction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import comp30022.server.RoutePlanning.RouteHash;
import comp30022.server.RoutePlanning.RoutePlanner;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class RoutePackageTests {

    @Test
    public void routeHashTest() {
        LatLng starts = new LatLng(37, 145);
        LatLng end = new LatLng(37.01, 145.01);
        LatLng[] origins = {starts};
        LatLng[] destinations = {end};

        int hash = RouteHash.hashOriginsDestinations(origins, destinations);
        assertNotNull(hash);
    }

    @Test
    public void routeHashTest2() {
        LatLng starts = new LatLng(37, 145);
        LatLng end = new LatLng(37.01, 145.01);
        LatLng[] origins = {};
        LatLng[] destinations = {};

        int hash = RouteHash.hashOriginsDestinations(origins, destinations);
        assertNotNull(hash);
    }

    @Test
    public void routeHashTest3() {
        LatLng starts = new LatLng(37, 145);
        LatLng end = new LatLng(37.01, 145.01);
        LatLng[] origins = {starts, starts};
        LatLng[] destinations = {end, end};

        int hash = RouteHash.hashOriginsDestinations(origins, destinations);
        assertNotNull(hash);
    }

    @Test
    public void getDirectionsTest(){
        LatLng starts = new LatLng(-37.7964, 144.9612);
        LatLng end = new LatLng(-37.8077, 144.9632);
        LatLng[] origins = {starts, starts};
        LatLng[] destinations = {end, end};

        RoutePlanner planner = new RoutePlanner(new GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build());
        try{
            DirectionsResult result = planner.getDirections(origins, destinations);
            assert(result.routes.length > 0);
        } catch(Exception e){
            assertEquals(1,2);
        }
    }
}
