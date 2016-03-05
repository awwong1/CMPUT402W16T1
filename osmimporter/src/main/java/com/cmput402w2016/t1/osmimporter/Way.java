package com.cmput402w2016.t1.osmimporter;

import java.util.ArrayList;

/**
 * Created by kent on 04/03/16.
 */
public class Way {
    long id;
    ArrayList<Node> geohash_nodes;

    Way() {
        geohash_nodes = new ArrayList<>();
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    void addNode(Node node) {
        geohash_nodes.add(node);
    }
}
