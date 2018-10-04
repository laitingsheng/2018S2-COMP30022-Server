package comp30022.server;

import com.google.maps.model.LatLng;
import comp30022.server.RoutePlanning.RouteHash;
import org.junit.Test;
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
}
