package com.cmput402w2016.t1.osmimporter;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

/**
 * Created by kent on 04/03/16.
 */
public class Node {
    long id;
    double lat;
    double lon;

    Node() {
        id = Long.MIN_VALUE;
        lat = Double.MIN_VALUE;
        lon = Double.MIN_VALUE;
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public boolean isComplete() {
        return lat != Double.MIN_VALUE && lon != Double.MIN_VALUE && id != Long.MIN_VALUE;
    }

    public void setLat(String lat) {
        this.lat = Double.parseDouble(lat);
    }

    public void setLon(String lon) {
        this.lon = Double.parseDouble(lon);
    }

    public String computeGeohash() {
        LatLong latLon = new LatLong(lat, lon);
        return GeoHash.encodeHash(latLon);
    }
}
