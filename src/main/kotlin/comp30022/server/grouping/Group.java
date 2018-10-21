package comp30022.server.grouping;

import com.google.cloud.firestore.GeoPoint;

public class Group {
    public final String groupId;
    public final GeoPoint groupLocation;
    public final String[] users;
    public final GeoPoint[] origins;
    public final GeoPoint[] destination;

    /**
     * The Group class used to pass information
     * @param groupId
     * @param groupLocation
     * @param users
     * @param origins
     * @param destinations
     */
    public Group(
        String groupId, GeoPoint groupLocation, String[] users, GeoPoint[] origins, GeoPoint[] destinations
    ) {
        this.groupId = groupId;
        this.groupLocation = groupLocation;
        this.users = users;
        this.origins = origins;
        this.destination = destinations;
    }
}
