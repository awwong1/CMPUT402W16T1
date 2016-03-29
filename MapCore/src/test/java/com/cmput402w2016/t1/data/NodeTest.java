package com.cmput402w2016.t1.data;

import junit.framework.TestCase;

public class NodeTest extends TestCase {

    public void testGetTagsAsSerializedJSON() throws Exception {
        Node node = new Node();
        node.addTag("Tag 1", "Value 1");
        node.addTag("Tag 2", "Value 2");
        String expectedVal = "{\"Tag 2\":\"Value 2\",\"Tag 1\":\"Value 1\",\"id\":\"-9223372036854775808\"}";
        assertEquals(expectedVal, node.getTagsAsSerializedJSON());
    }

    public void test_create_node_from_id_and_tags() {
        Node node = new Node("c3qt4j9u4vzf", "{\"id\":\"2791725287\",\"power\":\"tower\"}");
        String serialized_json = "" +
                "{\"geohash\":\"c3qt4j9u4vzf\"," +
                "\"lat\":52.94108656235039," +
                "\"lon\":-114.518952537328," +
                "\"id\":2791725287," +
                "\"tags\":{\"power\":\"\\\"tower\\\"\",\"id\":\"2791725287\"}}";
        assertEquals(serialized_json, node.toSerializedJson());
    }
}