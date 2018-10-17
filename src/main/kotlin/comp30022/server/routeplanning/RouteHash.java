package comp30022.server.routeplanning;

import com.google.cloud.firestore.GeoPoint;

import java.util.Arrays;

public class RouteHash {
    public static int hashOriginsDestinations(GeoPoint[] origins, GeoPoint[] destinations) {

        //Create Hashing
        int originsHashCode = Arrays.deepHashCode(origins);
        int desitnationsHashCode = Arrays.deepHashCode(destinations);

        //an additionaly layer of hashing as key value for result
        int odHashCode = Arrays.hashCode(new int[] {originsHashCode, desitnationsHashCode});

        return odHashCode;
    }
}