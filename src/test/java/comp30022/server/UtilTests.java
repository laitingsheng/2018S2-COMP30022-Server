package comp30022.server;

import com.google.cloud.firestore.GeoPoint;
import comp30022.server.utility.GeoHashing;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTests {

    @Test
    public void geoHashTest(){
        GeoPoint location = new GeoPoint(31, 145);
        String hash = GeoHashing.hash(location, 12);
        assertEquals(hash.length(), 12);
    }

    @Test
    public void geoHashTest2(){
        GeoPoint location = new GeoPoint(31, 145);
        String hash = GeoHashing.hash(location, 0);
        assertEquals(hash.length(), 0);
    }

    @Test
    public void geoHashTest3(){
        GeoPoint location = new GeoPoint(31, 145);
        String hash = GeoHashing.hash(location, 1);
        assertEquals(hash.length(), 1);
    }

}
