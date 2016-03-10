package com.cmput402w2016.t1.data;

/**
 * Created by kent on 09/03/16.
 */
public class Location {
    double lat;
    double lon;

    @Override
    public String toString() {
        return "(Lat: " + lat + ", Lon:" + lon + ")";
    }
}
