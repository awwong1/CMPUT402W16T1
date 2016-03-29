package com.cmput402w2016.t1.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.TestCase;

public class TrafficDataTest extends TestCase {

    public void test_json_to_traffic_data() throws Exception {
        String rawJsonObject = "{" +
                "\"from\": {" +
                "\"lon\": -113.506595," +
                "\"lat\": 53.5372492" +
                "}," +
                "\"to\": {" +
                "\"lon\": -113.5047471," +
                "\"lat\": 53.5372532" +
                "}," +
                "\"key\": \"KEY\"," +
                "\"time\": 1459269834," +
                "\"value\" : \"VALUE\"" +
                "}";
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(rawJsonObject);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        TrafficData trafficData = TrafficData.json_to_traffic_data(jsonObject);

        Location from = new Location("53.5372492", "-113.506595");
        Location to = new Location("53.5372532", "-113.5047471");
        String key = "KEY";
        String value = "VALUE";
        long time = 1459269834;
        TrafficData actualData = new TrafficData(from, to, time, key, value);
        System.out.println(actualData.equals(trafficData));
        assertEquals(actualData, trafficData);

    }
}