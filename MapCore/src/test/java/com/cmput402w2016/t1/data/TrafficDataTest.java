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
                "\"value\" : 34.25" +
                "}";
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(rawJsonObject);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        TrafficData trafficData = TrafficData.json_to_traffic_data(jsonObject);

        Location from = new Location("53.5372492", "-113.506595");
        Location to = new Location("53.5372532", "-113.5047471");
        String key = "KEY";
        Double value = 34.25;
        long time = 1459269834;
        TrafficData actualData = new TrafficData(from, to, time, key, value);
        assertEquals(actualData, trafficData);
    }

    public void test_json_to_traffic_data_with_geohash() throws Exception {
        String rawJsonObject = "{" +
                "\"from\":\"c3x2945j0ds7\"," +
                "\"to\":\"c3x294hm5eq7\"," +
                "\"timestamp\":1459269834," +
                "\"key\":\"KEY\"," +
                "\"value\":40.0" +
                "}";
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(rawJsonObject);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        TrafficData trafficData = TrafficData.json_to_traffic_data(jsonObject);

        Location from = new Location("c3x2945j0ds7");
        Location to = new Location("c3x294hm5eq7");
        String key = "KEY";
        Double value = 40.0;
        long time = 1459269834;
        TrafficData actualData = new TrafficData(from, to, time, key, value);
        assertEquals(actualData, trafficData);
    }

    public void test_traffic_data_valid() throws Exception {
        Location from = new Location("53.5372492", "-113.506595");
        Location to = new Location("53.5372532", "-113.5047471");
        String key = "KEY";
        Double value = 34.25;
        long time = 1459269834;
        TrafficData trafficData = new TrafficData(from, to, time, key, value);
        assertTrue(trafficData.isValid());

    }
}