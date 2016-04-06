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

/**
 * Java object representing a given OSM node
 */
public class Node {
    // OSM ID of the Node
    private long osmId = Long.MIN_VALUE;
    // Where the node is (lat/lon/geohash)
    private Location location = new Location(Double.MIN_VALUE, Double.MIN_VALUE);

    // The mapping of all the node tags defined in OSM
    private Map<String, String> tags = new HashMap<>();

    /**
     * Empty constructor
     */
    public Node() {
    }

    /**
     * Construct the node with a geohash only
     *
     * @param geohash String geohash
     */
    public Node(String geohash) {
        this.location = new Location(geohash);
    }

    /**
     * Construct the node with the lat lon only
     *
     * @param lat Double value of lat
     * @param lon Double value of lon
     */
    public Node(Double lat, Double lon) {
        this.location = new Location(lat, lon);
    }

    /**
     * Construct the node with the location only
     *
     * @param loc Location object, value of loc
     */
    public Node(Location loc) {
        this.location = loc;
    }

    /**
     * Custom equality for the Node object
     *
     * @param o Other object to compare to
     * @return Boolean, true if equals, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (osmId != node.osmId) return false;
        if (!location.equals(node.location)) return false;
        return tags.equals(node.tags);

    }

    /**
     * Custom hash code for the node object
     *
     * @return String hash code value of the node's location
     */
    @Override
    public int hashCode() {
        return location.hashCode();
    }

    /**
     * Constructor for the node with the full location and the serialized node tags
     *
     * @param geohash         Location of the current node
     * @param serialized_tags String with a json object for all the node tags
     */
    public Node(String geohash, String serialized_tags) {
        this.location = new Location(geohash);
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(serialized_tags);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement val = entry.getValue();
            if (key.equals("id")) {
                this.osmId = val.getAsLong();
            } else {
                this.tags.put(key, val.toString());
            }
        }
    }

    /**
     * Constructor for the node with the full location and the serialized node tags
     *
     * @param geohash Location of the current node
     * @param tags    Map of String:String containing all the node tags
     */
    public Node(String geohash, Map<String, String> tags) {
        this.location = new Location(geohash);
        this.tags.putAll(tags);
    }

    /**
     * Set the OSM ID
     *
     * @param osmId String value of the OSM id
     */
    public void setOsmId(String osmId) {
        this.osmId = Long.parseLong(osmId);
    }

    /**
     * Get the OSM ID
     *
     * @return long value of the OSM id
     */
    public long getOsmId() {
        return this.osmId;
    }

    /**
     * Set the node's lat
     *
     * @param lat String value of the lat to set
     */
    public void setLat(String lat) {
        this.location.setLat(Double.parseDouble(lat));
    }

    /**
     * Set the node's lon
     *
     * @param lon String value of the lon to set
     */
    public void setLon(String lon) {
        this.location.setLon(Double.parseDouble(lon));
    }

    /**
     * Get the node's lat
     *
     * @return Double value of the node's lat
     */
    public Double getLat() {
        return this.location.getLat();
    }

    /**
     * Get the node's lon
     *
     * @return Double value of the node's lon
     */
    public Double getLon() {
        return this.location.getLon();
    }

    /**
     * Check if the node contains all the necessary values for operations
     *
     * @return True if all required fields are set, false otherwise
     */
    public boolean isComplete() {
        return location.isValid() && osmId != Long.MIN_VALUE;
    }

    /**
     * Return the node's geohash
     *
     * @return String value of the node's geohash
     */
    public String getGeohash() {
        return location.getGeohash();
    }

    /**
     * Return the node's location
     *
     * @return Location object of the node's location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Add a tag to the node's tags
     *
     * @param key   Key of the tag, String
     * @param value Value of the tag, String
     */
    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    /**
     * Get all of the node's tags with the node id incorporated in the serialized json output
     *
     * @return String, serialized json output with node tags and ID
     */
    public String getTagsWithIDAsSerializedJSON() {
        Map<String, String> m_tags = new HashMap<>(this.tags);
        m_tags.put("id", String.valueOf(this.getOsmId()));
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
        json.addProperty("geohash", this.getGeohash());
        json.addProperty("lat", this.getLat());
        json.addProperty("lon", this.getLon());
        json.addProperty("osm_id", this.getOsmId());
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
        String[] neighbors = Segment.getNeighborGeohashesAsGeohashArray(this.getGeohash(), segment_table);
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

    /**
     * Return the boolean if the location is valid
     *
     * @return Boolean, true if location is valid, false otherwise
     */
    public boolean isValid() {
        return location.isValid();
    }
}
