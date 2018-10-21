package comp30022.server.util;

import com.google.cloud.firestore.GeoPoint;
import com.google.maps.model.LatLng;

/**
 * All kind of converter function that will be used in the server program
 */
public class Converter {

    /**
     * Convert firestore.Geopoint object to google.maps.model.LatLNG object
     * @param point: a GeoPOint object
     * @return: google.maps.model.LatLNG object object
     */
    public static LatLng geoToLatLng(GeoPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    /**
     * Convert a lsit of strings to A list of firestore.Geopoint Object
     * @param points: A list of String in "$latitude,$longitude" form
     * @return A list of GeoPoint
     */
    public static GeoPoint[] parseGeoPoints(String[] points) {
        GeoPoint[] geoPoints = new GeoPoint[points.length];
        for (int i = 0; i < points.length; i++) {
            geoPoints[i] = parseGeoPoint(points[i]);
        }
        return geoPoints;
    }

    /**
     * Convert string in "$latitude,$longitude" form to a firestore.GeoPoint object
     * @param point: A string in "$latitude,$longitude" form
     * @return A firestore.GeoPoint object
     */
    public static GeoPoint parseGeoPoint(String point) {
        String[] separated = point.split(",");
        // I suppose, this will contain latitude
        double latitudeE6 = Double.parseDouble(separated[0]);
        // I suppose, this will contain longitude
        double longitudeE6 = Double.parseDouble(separated[1]);

        return new GeoPoint(latitudeE6, longitudeE6);
    }
}
