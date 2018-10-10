package comp30022.server.util;

import com.google.cloud.firestore.GeoPoint;
import com.google.maps.model.LatLng;

public class Converter {

    public static LatLng geoToLatLng(GeoPoint point){
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    public static GeoPoint[] parseGeoPoints(String[] points){
        GeoPoint[] geoPoints = new GeoPoint[points.length];
        for(int i = 0; i < points.length; i++){
            geoPoints[i] = parseGeoPoint(points[i]);
        }
        return geoPoints;
    }

    public static GeoPoint parseGeoPoint(String point){
        String[] separated = point.split(",");
        // I suppose, this will contain latitude
        double latitudeE6 = Double.parseDouble(separated[0]);
        // I suppose, this will contain longitude
        double longitudeE6 = Double.parseDouble(separated[1]);

        return new GeoPoint(latitudeE6, longitudeE6);
    }

}
