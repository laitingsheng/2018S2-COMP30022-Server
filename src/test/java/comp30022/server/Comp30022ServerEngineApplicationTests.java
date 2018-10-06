package comp30022.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Comp30022ServerEngineApplicationTests {

    @Test
    public void routePlanningTest() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();

        String[] origins = new String[1];
        origins[0] = "-37.7984983,144.961";
        String[] destinations = new String[1];
        destinations[0] = "-37.79948794931647,144.96114693582058";

        HashMap<String, String[]> routes = new HashMap<String, String[]>();
        routes.put("origins", origins);
        routes.put("destinations", destinations);

        assertNotNull(application.routePlanning(routes));
    }

//    @Test
//    public void routePlanningTest2() {
//        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();
//        RoutePair routes = new RoutePair();
//        assertNull(application.routeplanning(routes));
//    }

    @Test
    public void routePlanningTest3() {
        Comp30022ServerEngineApplication application = new Comp30022ServerEngineApplication();


        String[] origins = new String[2];
        origins[0] = "-37.7984983,144.961";
        origins[1] = "-37.7984983,144.961";
        String[] destinations = new String[2];
        destinations[0] = "-37.79948794931647,144.96114693582058";
        destinations[1] = "-37.79948794931647,144.96114693582058";


        HashMap<String, String[]> routes = new HashMap<String, String[]>();
        routes.put("origins", origins);
        routes.put("destinations", destinations);

        assertNotNull(application.routePlanning(routes));
    }


}
