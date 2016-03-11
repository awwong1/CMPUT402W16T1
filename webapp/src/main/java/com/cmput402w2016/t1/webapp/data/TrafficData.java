package com.cmput402w2016.t1.webapp.data;

import com.cmput402w2016.t1.webapp.Location;
import org.apache.commons.lang.math.NumberUtils;

public class TrafficData {
    private Location from;
    private Location to;
    private String key;
    private String value;

    public TrafficData(Location from, Location to, String key, String value) {
        this.from = from;
        this.to = to;
        this.key = key;
        this.value = value;
    }

    public boolean isValid() {
        return from.isValid() && to.isValid() && NumberUtils.isNumber(value) && !key.contains("~");
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
