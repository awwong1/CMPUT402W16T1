package com.cmput402w2016.t1.data.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

/**
 * There was no plan for a segment originally, so this is a bit messy at the moment
 */
public class SegmentResponse {
    String from;
    Map<String, Map<String, String>> segmentNodes;

    public SegmentResponse(String json) {
        segmentNodes = new HashMap<>();
        JsonParser p = new JsonParser();
        JsonElement j = p.parse(json);
        JsonObject o = j.getAsJsonObject();
        for (Map.Entry<String, JsonElement> element : o.entrySet()) {
            if ("from".equals(element.getKey())) {
                // From position
                from = element.getValue().getAsString();
            } else {
                // Connected node
                // Add tags
                HashMap<String, String> tags = new HashMap<>();
                for (Map.Entry<String, JsonElement> element2 : element.getValue().getAsJsonObject().entrySet()) {
                    tags.put(element2.getKey(), element2.getValue().getAsString());
                }
                segmentNodes.put(element.getKey(), tags);
            }
        }
    }

    public Map<String, Map<String, String>> getSegmentNodes() {
        return segmentNodes;
    }

    public String getFrom() {
        return from;
    }
}
