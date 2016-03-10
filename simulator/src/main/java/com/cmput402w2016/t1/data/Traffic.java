package com.cmput402w2016.t1.data;

import org.apache.commons.lang.math.NumberUtils;

public class Traffic {
    private Location from;
    private Location to;
    private String key;
    private Double value;

    public boolean isValid() {
        return from.isValid() && to.isValid() && !key.contains("~");
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
