package comp30022.server.util;

import com.google.cloud.firestore.GeoPoint;
import com.google.maps.model.LatLng;

public class GeoToLat {
    public static LatLng getoToLatLng(GeoPoint point){
        return new LatLng(point.getLatitude(), point.getLongitude());
    }
}
