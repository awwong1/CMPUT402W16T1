package com.cmput402w2016.t1.data;

import com.cmput402w2016.t1.util.Util;
import com.google.gson.Gson;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Node {
    // OSM ID of the Node
    private long id = Long.MIN_VALUE;
    // Where the node is (lat/lon/geohash)
    private Location location = new Location(Double.MIN_VALUE, Double.MIN_VALUE);
    // The mapping of all the node tags defined in OSM
    private Map<String, String> tags = new HashMap<>();

    public Node() {
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public long getId() {
        return this.id;
    }

    public void setLat(String lat) {
        this.location.setLat(Double.parseDouble(lat));
    }

    public void setLon(String lon) {
        this.location.setLon(Double.parseDouble(lon));
    }

    public Double getLat() {
        return this.location.getLat();
    }

    public Double getLon() {
        return this.location.getLon();
    }

    public boolean isComplete() {
        return location.isValid() && id != Long.MIN_VALUE;
    }

    public String computeGeohash() {
        return location.computeGeohash();
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public String getTagsAsSerializedJSON() {
        tags.put("id", String.valueOf(this.getId()));
        Gson gson = new Gson();
        return gson.toJson(tags);
    }

    /**
     * For a given node, return its neighbor nodes.
     *
     * @param segment_table HBase Table of node segments
     * @return
     */
    public Node[] getNeighborGeohashes(Table segment_table) {
        // TODO
        return new Node[]{};
    }

    public String getClosestNeighborGeohash(Table segment_table, String neighbor_hash) {
        // TODO
        return "";
    }

    /**
     * Given a location, return the closest node to that given location.
     *
     * @param location   Location object representing where
     * @param node_table HBase table containing the nodes
     * @return
     */
    public static Node getClosestNode(Location location, Table node_table) {
        String geoHash = location.computeGeohash();
        while (geoHash != null) {
            Scan scan = new Scan();
            scan.setRowPrefixFilter(geoHash.getBytes());
            try {
                ResultScanner rs = node_table.getScanner(scan);
                Result result = rs.next();
                if (result != null) {
                    return getNodeFromGeohash(new String(result.getRow()), node_table);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            geoHash = Util.shorten(geoHash);
        }
        return new Node();
    }

    public static Node getNodeFromGeohash(String geohash, Table node_table) {
        // TODO
        Get g = new Get(Bytes.toBytes(geohash));
        try {
            Result r = node_table.get(g);
            System.out.println(r.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Node();
    }
}