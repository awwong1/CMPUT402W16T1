package com.cmput402w2016.t1.osmimporter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kent on 04/03/16.
 */
public class Way {
    long id;
    ArrayList<Node> geohash_nodes;
    HashMap<String, String> way_tags = new HashMap<String, String>();

    Way() {
        geohash_nodes = new ArrayList<Node>();
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    void addNode(Node node) {
        geohash_nodes.add(node);
    }

    void addTag(String t_k, String t_v) {
        way_tags.put(t_k, t_v);
    }
}
