package com.cmput402w2016.t1.data.controller;

import com.cmput402w2016.t1.data.Location;
import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * Interacts via. a direct connection toNode Hadoop & HBase
 */
public class HBaseController implements DatabaseController {
    public void postTraffic(TrafficData traffic) {
        throw new NotImplementedException();
    }

    public Node getClosestNode(Location location) {
        throw new NotImplementedException();
    }

    public SegmentResponse getSegment(Location location) {
        throw new NotImplementedException();
    }
}
