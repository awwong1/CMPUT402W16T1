package com.cmput402w2016.t1.converter;

import com.cmput402w2016.t1.data.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kent on 4/2/2016.
 */
public class SimulatorData {
    // Holds traffic counts of data, timestamp -> count
    protected Map<String, Integer> traffic;
    protected String source;
    protected Node from;
    protected Node to;

    public SimulatorData(Node from, Node to, String source) {
        this.from = from;
        this.to = to;
        this.source = source;
        traffic = new HashMap<>();
    }
}
