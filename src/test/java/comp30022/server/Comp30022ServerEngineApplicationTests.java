package comp30022.server;

import com.google.maps.model.LatLng;
import comp30022.server.RoutePlanning.RoutePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Comp30022ServerEngineApplicationTests {

    @Test
    public void routePlanningTest() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
        RoutePair routes = new RoutePair();
        LatLng starts = new LatLng(37, 145);
        LatLng end = new LatLng(37.01, 145.01);
        routes.origins = new LatLng[1];
        routes.origins[0] = starts;
        routes.destinations = new LatLng[1];
        routes.destinations[0] = end;
        assertNotNull(application.routePlanning(routes));
    }

    @Test(expected = java.lang.NullPointerException.class)
    public void routePlanningTest2() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
        RoutePair routes = new RoutePair();
        assertNotNull(application.routePlanning(routes));
    }

    @Test
    public void routePlanningTest3() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
        RoutePair routes = new RoutePair();
        LatLng starts = new LatLng(37, 145);
        LatLng end = new LatLng(37.01, 145.01);
        routes.origins = new LatLng[2];
        routes.origins[0] = starts;
        routes.origins[1] = starts;
        routes.destinations = new LatLng[2];
        routes.destinations[0] = end;
        routes.destinations[1] = end;
        assertNotNull(application.routePlanning(routes));
    }


}
