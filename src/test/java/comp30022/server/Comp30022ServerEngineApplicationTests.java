package comp30022.server;

import com.google.cloud.firestore.GeoPoint;
import comp30022.server.RoutePlanning.RoutePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Comp30022ServerEngineApplicationTests {

    @Test
    public void routePlanningTest() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
        RoutePair routes = new RoutePair();
        GeoPoint starts = new GeoPoint(37, 145);
        GeoPoint end = new GeoPoint(37.01, 145.01);
        routes.origins = new GeoPoint[1];
        routes.origins[0] = starts;
        routes.destinations = new GeoPoint[1];
        routes.destinations[0] = end;
        assertNotNull(application.routePlanning(routes));
    }

//    @Test
//    public void routePlanningTest2() {
//        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
//        RoutePair routes = new RoutePair();
//        assertNull(application.routePlanning(routes));
//    }

    @Test
    public void routePlanningTest3() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
        RoutePair routes = new RoutePair();
        GeoPoint starts = new GeoPoint(37, 145);
        GeoPoint end = new GeoPoint(37.01, 145.01);
        routes.origins = new GeoPoint[2];
        routes.origins[0] = starts;
        routes.origins[1] = starts;
        routes.destinations = new GeoPoint[2];
        routes.destinations[0] = end;
        routes.destinations[1] = end;
        assertNotNull(application.routePlanning(routes));
    }


}
