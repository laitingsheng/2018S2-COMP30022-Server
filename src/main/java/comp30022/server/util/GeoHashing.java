package comp30022.server.util;

import ch.hsr.geohash.GeoHash;
import com.google.cloud.firestore.GeoPoint;

public class GeoHashing {
    private static final int PRECISION = 12;

    public static String hash(GeoPoint location, int precisionLevel) {
        // precisionLevel: 8 characters for around 200m of precision

        GeoHash geohash = GeoHash.withCharacterPrecision(location.getLatitude(), location.getLongitude(), PRECISION);
        String resultString = geohash.toBase32().substring(0, precisionLevel);
        return resultString;
    }
}