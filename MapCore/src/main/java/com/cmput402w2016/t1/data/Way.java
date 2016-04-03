package com.cmput402w2016.t1.data;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Way {
    // OSM ID for the way
    private long id;
    // Ordered List of Nodes
    private List<Node> geohash_nodes;
    // OSM defined tags for the ways
    private Map<String, String> tags = new HashMap<>();

    public Way() {
        geohash_nodes = new ArrayList<>();
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public long getId() {
        return this.id;
    }

    public void addNode(Node node) {
        geohash_nodes.add(node);
    }

    public List<Node> getNodes() {
        return geohash_nodes;
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public String getTagsAsSerializedJSON() {
        tags.put("id", String.valueOf(this.getId()));
        Gson gson = new Gson();
        return gson.toJson(tags);
    }

}