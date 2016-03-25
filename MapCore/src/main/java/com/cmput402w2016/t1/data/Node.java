package com.cmput402w2016.t1.data;


import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

public class Node {
    long id;
    double lat;
    double lon;
    String geohash = null;

    public Node() {
        id = Long.MIN_VALUE;
        lat = Double.MIN_VALUE;
        lon = Double.MIN_VALUE;
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public long getId() {
        return this.id;
    }

    public boolean isComplete() {
        return lat != Double.MIN_VALUE && lon != Double.MIN_VALUE && id != Long.MIN_VALUE;
    }

    public void setLat(String lat) {
        this.lat = Double.parseDouble(lat);
    }

    public double getLat() {
        return this.lat;
    }

    public void setLon(String lon) {
        this.lon = Double.parseDouble(lon);
    }

    public double getLon() {
        return this.lon;
    }

    public String computeGeohash() {
        if (geohash == null) {
            LatLong latLon = new LatLong(lat, lon);
            geohash = GeoHash.encodeHash(latLon);
        }
        return geohash;
    }
}