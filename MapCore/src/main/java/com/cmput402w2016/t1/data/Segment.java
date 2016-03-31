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

public class Segment {
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
    
    public static String[] getNeighborGeohashesAsGeohashArray(String start_node_geohash, Table segment_table) {
        Map<String, String> hmap = getNeighborGeohashesAsStringMap(start_node_geohash, segment_table);
        if (hmap == null) {
            return null;
        }
        return hmap.keySet().toArray(new String[]{});
    }

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

    public static String getClosestSegmentFromLatLon(String lat, String lon, Table segment_table) {
        return getClosestSegmentFromGeohash(new Location(lat, lon).getGeohash(), segment_table);
    }
}
