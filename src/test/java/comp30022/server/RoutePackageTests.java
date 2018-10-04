package comp30022.server;

import com.google.cloud.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import comp30022.server.RoutePlanning.RouteHash;
import comp30022.server.RoutePlanning.RoutePlanner;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class RoutePackageTests {

    @Test
    public void routeHashTest() {
        GeoPoint starts = new GeoPoint(37, 145);
        GeoPoint end = new GeoPoint(37.01, 145.01);
        GeoPoint[] origins = {starts};
        GeoPoint[] destinations = {end};

        int hash = RouteHash.hashOriginsDestinations(origins, destinations);
        assertNotNull(hash);
    }

    @Test
    public void routeHashTest2() {
        GeoPoint starts = new GeoPoint(37, 145);
        GeoPoint end = new GeoPoint(37.01, 145.01);
        GeoPoint[] origins = {};
        GeoPoint[] destinations = {};

        int hash = RouteHash.hashOriginsDestinations(origins, destinations);
        assertNotNull(hash);
    }

    @Test
    public void routeHashTest3() {
        GeoPoint starts = new GeoPoint(37, 145);
        GeoPoint end = new GeoPoint(37.01, 145.01);
        GeoPoint[] origins = {starts, starts};
        GeoPoint[] destinations = {end, end};

        int hash = RouteHash.hashOriginsDestinations(origins, destinations);
        assertNotNull(hash);
    }

    @Test
    public void getDirectionsTest(){
        GeoPoint starts = new GeoPoint(-37.7964, 144.9612);
        GeoPoint end = new GeoPoint(-37.8077, 144.9632);
        GeoPoint[] origins = {starts, starts};
        GeoPoint[] destinations = {end, end};

        RoutePlanner planner = new RoutePlanner(new GeoApiContext.Builder().apiKey(Constant.GOOGLEMAPAPIKEY).build());
        try{
            DirectionsResult result = planner.getDirections(origins, destinations);
            assert(result.routes.length > 0);
        } catch(Exception e){
            assertEquals(1,2);
        }
    }
}
