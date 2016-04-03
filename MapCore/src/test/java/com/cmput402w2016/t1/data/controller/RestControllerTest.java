package com.cmput402w2016.t1.data.controller;

import com.cmput402w2016.t1.data.Location;
import com.cmput402w2016.t1.data.Node;
import junit.framework.TestCase;
import static org.junit.Assert.*;

/**
 * Created by Kent on 4/2/2016.
 */
public class RestControllerTest extends TestCase {
    RestController controller;

    public void setUp() throws Exception {
        super.setUp();
        controller = new RestController("http://199.116.235.225");
    }

    public void testGetClosestNode1() throws Exception {
        Location input_location = new Location("c3x21hyb5b59");
        Node closestNode = controller.getClosestNode(input_location);
        String retrieved_location = closestNode.getGeohash();

        // Should match with a node, but that node's location should be different from our test
        // unless OSM & our database get updated.
        assertNotEquals(retrieved_location, input_location.getGeohash());

        // Should match with this node, unless OSM & our database get updated.
        Location expected_location = new Location("c3x21hyb5b53");
        assertEquals(retrieved_location, expected_location);
    }
}