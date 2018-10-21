package comp30022.server.routeplanning;

import com.google.cloud.firestore.GeoPoint;

import java.util.Arrays;

public class RouteHash {
    /**
     * This is the hash function to create key for route request, in order to cash the result on FIrebase
     * @param origins
     * @param destinations
     * @return int, the key for caching result
     */
    public static int hashOriginsDestinations(GeoPoint[] origins, GeoPoint[] destinations) {

        //Create Hashing
        int originsHashCode = Arrays.deepHashCode(origins);
        int desitnationsHashCode = Arrays.deepHashCode(destinations);

        //an additionaly layer of hashing as key value for result
        int odHashCode = Arrays.hashCode(new int[] {originsHashCode, desitnationsHashCode});

        return odHashCode;
    }
}
