package com.cmput402w2016.t1.webapp.data;

import com.cmput402w2016.t1.webapp.Location;
import org.apache.commons.lang.math.NumberUtils;

public class TrafficData {
    private Location from;
    private Location to;
    // This is supposed to be an epoch time (seconds since epoch), not java's milliseconds
    private long timestamp;
    private String key;
    private String value;

    public TrafficData(Location from, Location to, long timestamp, String key, String value) {
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
    }

    public boolean isValid() {
        // TODO, no timestamps greater than one hour in the future (maybe?)
        return from.isValid() && to.isValid() && timestamp > 0 && NumberUtils.isNumber(value) && !key.contains("~");
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
