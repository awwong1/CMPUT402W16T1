package com.cmput402w2016.t1.data.controller;

import com.cmput402w2016.t1.data.Location;
import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;

/**
 * Specifications for functionality that we need to implement so that we can post/get data to/from our database
 */
public interface DatabaseController {
    /**
     * @param traffic Traffic Object containing data to be sent to database
     */
    void postTraffic(TrafficData traffic);

    Node getClosestNode(Location location);
}
