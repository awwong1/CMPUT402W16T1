package com.cmput402w2016.t1.data;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for the Way
 */
public class Way {
    // OSM ID for the way
    private long id;
    // Ordered List of Nodes
    private List<Node> geohash_nodes;
    // OSM defined tags for the ways
    private Map<String, String> tags = new HashMap<>();

    /**
     * Constructor for the way, set an empty list of geohash nodes
     */
    public Way() {
        geohash_nodes = new ArrayList<>();
    }

    /**
     * Set the way's OSM id
     *
     * @param id String value of the osm ID
     */
    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    /**
     * Get the way's ID
     *
     * @return long, value of the osm ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * Add Node to the list of geohash nodes
     *
     * @param node Node object to be added
     */
    public void addNode(Node node) {
        geohash_nodes.add(node);
    }

    /**
     * Get the list of nodes associated with this way
     *
     * @return List of nodes
     */
    public List<Node> getNodes() {
        return geohash_nodes;
    }

    /**
     * Add a tag to the way
     *
     * @param key   String key
     * @param value String value
     */
    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    /**
     * Get the tags as serialized JSON
     *
     * @return String, value of all the tags as serialized JSON
     */
    public String getTagsAsSerializedJSON() {
        tags.put("id", String.valueOf(this.getId()));
        Gson gson = new Gson();
        return gson.toJson(tags);
    }

}