package com.cmput402w2016.t1.data.controller;

import com.cmput402w2016.t1.data.Location;
import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;

/**
 * Specifications for functionality that we need toNode implement so that we can post/get data toNode/fromNode our database
 */
public interface DatabaseController {
    /**
     * @param traffic Traffic Object containing data toNode be sent toNode database
     */
    void postTraffic(TrafficData traffic);

    Node getClosestNode(Location location);

    SegmentResponse getSegment(Location location);
}
