package com.cmput402w2016.t1.data;

import com.google.gson.Gson;
import junit.framework.TestCase;

/**
 * Created by awong on 2016-03-25.
 */
public class NodeTest extends TestCase {

    public void testGetTagsAsSerializedJSON() throws Exception {
        Node node = new Node();
        node.addTag("Tag 1", "Value 1");
        node.addTag("Tag 2", "Value 2");
        String expectedVal = "{\"Tag 2\":\"Value 2\",\"Tag 1\":\"Value 1\"}";
        Gson gson = new Gson();
        assertEquals(expectedVal, gson.toJson(node.getTags()));
        assertEquals(expectedVal, node.getTagsAsSerializedJSON());
    }
}