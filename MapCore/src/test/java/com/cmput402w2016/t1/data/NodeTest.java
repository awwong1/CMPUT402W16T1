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
}