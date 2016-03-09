package com.cmput402w2016.t1.osmimporter;

import java.util.ArrayList;

public class Way {
    long id;
    ArrayList<Node> geohash_nodes;

    Way() {
        geohash_nodes = new ArrayList<Node>();
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    void addNode(Node node) {
        geohash_nodes.add(node);
    }
}
