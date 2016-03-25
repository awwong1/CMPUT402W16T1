package com.cmput402w2016.t1.data;


import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private long id;
    private double lat;
    private double lon;
    private String geohash = null;
    private Map<String, String> tags = new HashMap<>();

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

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public Map<String, String> getTags() {
        return this.tags;
    }

    public String getTagsAsSerializedJSON() {
        Gson gson = new Gson();
        return gson.toJson(this.getTags());
    }
}