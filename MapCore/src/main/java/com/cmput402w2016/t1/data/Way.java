package com.cmput402w2016.t1.data;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Way {
    private long id;
    private ArrayList<Node> geohash_nodes;
    private Map<String, String> tags = new HashMap<String, String>();

    public Way() {
        geohash_nodes = new ArrayList<Node>();
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public void addNode(Node node) {
        geohash_nodes.add(node);
    }

    public ArrayList<Node> getNodes() {
        return geohash_nodes;
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