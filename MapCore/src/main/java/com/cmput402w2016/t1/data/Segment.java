package com.cmput402w2016.t1.data;

import com.cmput402w2016.t1.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

/**
 * Segment class containing static methods for generating segments
 */
public class Segment {
    /**
     * Get all the neighbors of the start node as a map of nodes with tags
     *
     * @param start_node_geohash String representation of the node
     * @param segment_table      HBase table of all the segments
     * @return Map of String, String representation of the neighbors and nodes
     */
    private static Map<String, String> getNeighborGeohashesAsStringMap(String start_node_geohash, Table segment_table) {
        Map<String, String> hmap = new HashMap<>();
        try {
            Get get = new Get(Bytes.toBytes(start_node_geohash));
            Result r = segment_table.get(get);
            if (r != null) {
                NavigableMap<byte[], byte[]> nm = r.getFamilyMap(Bytes.toBytes("node"));
                if (nm != null) {
                    for (Map.Entry<byte[], byte[]> entry : nm.entrySet()) {
                        String key = Bytes.toString(entry.getKey());
                        String value = Bytes.toString(entry.getValue());
                        hmap.put(key, value);
                    }
                    return hmap;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all the neighbors of the start node as a character array with tags
     *
     * @param start_node_geohash String representation of the ndoe
     * @param segment_table      HBase table of all the segments
     * @return String array of the node neighbors
     */
    public static String[] getNeighborGeohashesAsGeohashArray(String start_node_geohash, Table segment_table) {
        Map<String, String> hmap = getNeighborGeohashesAsStringMap(start_node_geohash, segment_table);
        if (hmap == null) {
            return null;
        }
        return hmap.keySet().toArray(new String[]{});
    }

    /**
     * Transform the start node and the neighbors into a json object
     *
     * @param startNode String representation of the start node
     * @param hmap      String, String map of the neighbor and tags
     * @return JsonObject of all the neighbors and the start node
     */
    public static JsonObject transformSegmentMapToJsonObject(String startNode, Map<String, String> hmap) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("from", startNode);
        for (Map.Entry<String, String> e : hmap.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(value);
            jsonObject.add(key, jsonElement);
        }
        return jsonObject;
    }

    /**
     * Get the closest node from the string geohash
     *
     * @param original_geohash String representation of the original location as geohash
     * @param segment_table    HBase table of the segments
     * @return String, value of the closest node to the start location
     */
    public static String getClosestSegmentFromGeohash(String original_geohash, Table segment_table) {
        String geohash = original_geohash;
        try {
            while (geohash != null) {
                Scan scan = new Scan();
                scan.setRowPrefixFilter(Bytes.toBytes(geohash));
                ResultScanner rs = segment_table.getScanner(scan);
                Result r = rs.next();
                if (r != null) {
                    String start_node = Bytes.toString(r.getRow());
                    NavigableMap<byte[], byte[]> nm = r.getFamilyMap(Bytes.toBytes("node"));
                    if (nm != null) {
                        Map<String, String> hmap = new HashMap<>();
                        for (Map.Entry<byte[], byte[]> entry : nm.entrySet()) {
                            String key = Bytes.toString(entry.getKey());
                            String value = Bytes.toString(entry.getValue());
                            hmap.put(key, value);
                        }
                        return transformSegmentMapToJsonObject(start_node, hmap).toString();
                    }
                }
                geohash = Util.shorten(geohash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the closest node from the lat lon
     *
     * @param lat           String value of the lat
     * @param lon           String value of the lon
     * @param segment_table HBase table of the segments
     * @return String value of the closest node to the start location
     */
    public static String getClosestSegmentFromLatLon(String lat, String lon, Table segment_table) {
        return getClosestSegmentFromGeohash(new Location(lat, lon).getGeohash(), segment_table);
    }
}
