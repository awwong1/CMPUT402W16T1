package com.cmput402w2016.t1.data.controller;

import java.util.Map;

/**
 * The object returned by the REST server doesn't match up with the Node class 1:1
 */
public class NodeResponse {
    String geohash;
    Double lat;
    Double lon;
    Long osm_id;
    Map<String, String> tags;
}
