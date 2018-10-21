package comp30022.server.routeplanning;

import com.google.cloud.firestore.GeoPoint;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import comp30022.server.util.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class for call google Direction's API to provide navigation result
 */
public class RoutePlanner {

    private static final Logger LOGGER = Logger.getLogger(RoutePlanner.class.getName());
    private GeoApiContext geoApiContext;

    public RoutePlanner(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    /**
     * Call Google Direction's API to get the navigation result
     * @param origins Array of People's star position (GeoPoint)
     * @param destinations Array of People's end position (GeoPoint)
     * @return DirectionsResult, see google's document for detail of this class
     * @throws Exception
     */
    public DirectionsResult getDirections(GeoPoint[] origins, GeoPoint[] destinations) throws Exception {
        LatLng[] wayPoints = combineWayPoints(origins, destinations);

        DirectionsResult result = DirectionsApi
            .newRequest(geoApiContext)
            .mode(TravelMode.WALKING)
            .origin(Converter.geoToLatLng(origins[0]))
            .destination(Converter.geoToLatLng(destinations[0]))
            .waypoints(wayPoints)
            .optimizeWaypoints(true)
            .await();
        return result;
    }

    /**
     * Combine all people's origins and destination into waypoints, so that in navigation API google can optimise the
     * route
     * @param origins Array of People's star position (GeoPoint)
     * @param destinations Array of People's end position (GeoPoint)
     * @return A list of LatLng object that will be used for wayPOints in Google Direction's API
     */
    private LatLng[] combineWayPoints(GeoPoint[] origins, GeoPoint[] destinations) {
        List<LatLng> wayPoints = new ArrayList<>();
        for (GeoPoint o : origins) {
            wayPoints.add(Converter.geoToLatLng(o));
        }
        for (GeoPoint d : destinations) {
            wayPoints.add(Converter.geoToLatLng(d));
        }

        LatLng[] result = new LatLng[wayPoints.size()];
        return wayPoints.toArray(result);
    }
}
