package com.cmput402w2016.t1.data;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class Traffic {
    /**
     * Where the traffic is originating from
     */
    private Node from;
    /**
     * Where the traffic is heading to
     */
    private Node to;
    /**
     * Key-value pairs of data to store
     */
    private Map<String, Double> data;

    /**
     * Time of observation in seconds since epoch in UTC
     */
    private long timestamp;

    public Traffic(Node from, Node to, long timestamp) {
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;

        data = new HashMap<>();
    }

    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    /**
     * @param key   Traffic Data Key
     * @param value Traffic Data Value
     */
    public void addData(String key, Double value) {
        data.put(key, value);
    }

    /**
     * Remove all traffic data
     */
    public void removeAllData() {
        data.clear();
    }

    /**
     * @return true if the traffic data conforms to database specifications, false otherwise
     */
    public boolean isValid() {
        // TODO
        return true;
    }
}
