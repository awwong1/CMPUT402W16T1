package com.cmput402w2016.t1.data;

import com.cmput402w2016.t1.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
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

    public Node(String geohash, String serialized_tags) {
        this.location = new Location(geohash);
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(serialized_tags);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement val = entry.getValue();
            if (key.equals("id")) {
                this.id = val.getAsLong();
            } else {
                this.tags.put(key, val.toString());
            }
        }
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
        return location.getGeohash();
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public String getTagsWithIDAsSerializedJSON() {
        Map<String, String> m_tags = new HashMap<>(this.tags);
        m_tags.put("id", String.valueOf(this.getId()));
        Gson gson = new Gson();
        return gson.toJson(m_tags);
    }


    /**
     * Return the node as a serialized json string, matching the documentation specified in the project wiki
     *
     * @return String, current node as a serialized json object
     */
    public String toSerializedJson() {
        JsonObject json = new JsonObject();
        json.addProperty("geohash", this.computeGeohash());
        json.addProperty("lat", this.getLat());
        json.addProperty("lon", this.getLon());
        json.addProperty("osm_id", this.getId());
        JsonObject tags = new JsonObject();
        for (Map.Entry<String, String> e : this.tags.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            tags.addProperty(key, value);
        }
        json.add("tags", tags);
        return json.toString();
    }

    /**
     * Return the string geohash of the closest neighbor to the actual specified location.
     *
     * @param segment_table HBase table containing the
     * @param actual        Location that the user provided
     * @return String geohash representing the closet neighbor to the current node. Null if no neighbors.
     */
    public String getClosestNeighborGeohash(Table segment_table, Location actual) {
        String[] neighbors = Segment.getNeighborGeohashesAsGeohashArray(this.computeGeohash(), segment_table);
        String closest_neighbor = null;
        if (neighbors != null) {
            double distance = Double.MAX_VALUE;
            for (String neighbor : neighbors) {
                double temp_distance = actual.distance(new Location(neighbor));
                if (Double.compare(temp_distance, distance) < 0) {
                    distance = temp_distance;
                    closest_neighbor = neighbor;
                }
            }
        }
        return closest_neighbor;
    }

    /**
     * Given a location object, return the closest node to that location.
     *
     * @param location   Location object representing the lat and lon
     * @param node_table HBase table containing the nodes
     * @return Node object, null if node doesn't exist
     */
    public static Node getClosestNodeFromLocation(Location location, Table node_table) {
        return getClosestNodeFromGeohash(location.getGeohash(), node_table);
    }

    /**
     * Given a lat and lon, return the closest node to that given location.
     *
     * @param lat        String representing lat
     * @param lon        String representing lon
     * @param node_table HBase table containing the nodes
     * @return Node object, null if node doesn't exist
     */
    public static Node getClosestNodeFromLatLon(String lat, String lon, Table node_table) {
        Location location = new Location(lat, lon);
        String geoHash = location.getGeohash();
        return getClosestNodeFromGeohash(geoHash, node_table);
    }

    /**
     * Take a geohash, get the node object with all fields populated.
     *
     * @param original_geohash String geohash representing the node (substring search)
     * @param node_table       HBase table where all the nodes are stored
     * @return Node object, null if node doesn't exist
     */
    public static Node getClosestNodeFromGeohash(String original_geohash, Table node_table) {
        String geohash = original_geohash;
        try {
            while (geohash != null) {
                Scan scan = new Scan();
                scan.setRowPrefixFilter(Bytes.toBytes(geohash));
                ResultScanner rs = node_table.getScanner(scan);
                Result r = rs.next();
                if (r != null) {
                    String actual_geohash = Bytes.toString(r.getRow());
                    String tags = Bytes.toString(r.getValue(Bytes.toBytes("data"), Bytes.toBytes("tags")));
                    return new Node(actual_geohash, tags);
                }
                geohash = Util.shorten(geohash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Take an osm node id, get the node object with all fields populated
     *
     * @param osm_id     String osm id matching the node exactly
     * @param node_table HBase table where all the nodes are stored
     * @return Node object, null if node doesn't exist
     */
    public static Node getNodeFromID(String osm_id, Table node_table) {
        try {
            Scan scan = new Scan();
            scan.setFilter(new SingleColumnValueFilter(
                    Bytes.toBytes("data"),
                    Bytes.toBytes("osm_id"),
                    CompareFilter.CompareOp.EQUAL,
                    Bytes.toBytes(osm_id)));
            ResultScanner rs = node_table.getScanner(scan);
            Result r = rs.next();
            if (r != null) {
                String actual_geohash = Bytes.toString(r.getRow());
                String tags = Bytes.toString(r.getValue(Bytes.toBytes("data"), Bytes.toBytes("tags")));
                return new Node(actual_geohash, tags);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}