package com.cmput402w2016.t1.data.controller;

import junit.framework.TestCase;

public class SegmentResponseTest extends TestCase {
    public void testConstructor() throws Exception {
        String json = "{\n" +
                "   \"fromNode\":\"c3x21hyb5b53\",\n" +
                "   \"c3x21hrrvhs7\":{\n" +
                "      \"id\":\"25331556\",\n" +
                "      \"created_by\":\"Potlatch 0.9c\",\n" +
                "      \"highway\":\"residential\",\n" +
                "      \"name\":\"105A Street NW\"\n" +
                "   },\n" +
                "   \"c3x21kf0wexu\":{\n" +
                "      \"id\":\"25331498\",\n" +
                "      \"highway\":\"residential\",\n" +
                "      \"name\":\"31 Avenue NW\"\n" +
                "   },\n" +
                "   \"c3x21hxhbw93\":{\n" +
                "      \"id\":\"25331556\",\n" +
                "      \"created_by\":\"Potlatch 0.9c\",\n" +
                "      \"highway\":\"residential\",\n" +
                "      \"name\":\"105A Street NW\"\n" +
                "   }\n" +
                "}";
        SegmentResponse response = new SegmentResponse(json);

        // Should have created 3 segments
        assertTrue(response.segmentNodes.size() == 3);
        // Should contain the right data
        assertEquals(response.from, "c3x21hyb5b53");
        assertTrue(response.segmentNodes.containsKey("c3x21hrrvhs7"));
        assertTrue(response.segmentNodes.containsKey("c3x21kf0wexu"));
        assertTrue(response.segmentNodes.containsKey("c3x21hxhbw93"));
    }
}