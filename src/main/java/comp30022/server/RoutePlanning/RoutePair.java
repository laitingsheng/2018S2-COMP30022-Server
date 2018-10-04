package comp30022.server.RoutePlanning;

import com.google.cloud.firestore.GeoPoint;

import java.io.Serializable;

public class RoutePair implements Serializable {
    public String[] origins;
    public String[] destinations;
}



