package comp30022.server.util;

import ch.hsr.geohash.GeoHash;
import com.google.cloud.firestore.GeoPoint;

/**
 * GeoHashing class that hash a user's coordinate to string using GeoHashing Algorithm
 * THis is used for our grouping algorithm to find the nearby points
 */
public class GeoHashing {
    private static final int PRECISION = 12;

    /**
     * Hash a coordinate to string using GeoHashing algorithm
     * A precision of 8 means 200m's precision.
     * So given a coordinate, all coordinate within 200m's radius will have the same hash result
     * @param location A GeoPoint represent location
     * @param precisionLevel how precise the hashing will be. eg: 8 characters is 200m's precision.
     * @return the hash string of the location
     */
    public static String hash(GeoPoint location, int precisionLevel) {
        // precisionLevel: 8 characters for around 200m of precision

        GeoHash geohash = GeoHash.withCharacterPrecision(location.getLatitude(), location.getLongitude(), PRECISION);
        String resultString = geohash.toBase32().substring(0, precisionLevel);
        return resultString;
    }
}
