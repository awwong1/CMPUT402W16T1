package com.cmput402w2016.t1.webapp.data;

import com.cmput402w2016.t1.webapp.Location;
import org.apache.commons.lang.math.NumberUtils;

public class TrafficData {
    private Location from;
    private Location to;
    private String key;
    private String value;

    public boolean isValid() {
        return from.isValid() && to.isValid() && NumberUtils.isNumber(value) && !key.contains("~");
    }
    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
