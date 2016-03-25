package com.cmput402w2016.t1.data;

import java.util.ArrayList;

public class Way {
    long id;
    ArrayList<Node> geohash_nodes;

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
}