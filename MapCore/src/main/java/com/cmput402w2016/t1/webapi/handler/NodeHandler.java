package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.webapi.Helper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class NodeHandler implements HttpHandler {
    public NodeHandler() {
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        // Get & parse query
        String query = httpExchange.getRequestURI().getRawQuery();
        System.out.println(query); // DEBUG
        Map<String, String> stringStringMap = Helper.queryToMap(query);
        for (String key : stringStringMap.keySet()) {
            System.out.println("Key: " + key + ", Value: " + stringStringMap.get(key));
        }

        if (stringStringMap.containsKey("id")) {
            // TODO: Query by OSM Id
            Double osmId = Double.parseDouble(stringStringMap.get("id"));

        } else if (stringStringMap.containsKey("geohash")) {
            // TODO: Query by Geohash
            String geohash = stringStringMap.get("geohash");

        } else if (stringStringMap.containsKey("lat") && stringStringMap.containsKey("lon")) {
            // TODO: Query by latitude and longitude
            String latitude = stringStringMap.get("lat");
            String longitude = stringStringMap.get("lon");

        } else {
            // Unknown Request
            Helper.malformedRequestResponse(httpExchange, 400, "Missing parameter. Use 'id', 'geohash' or 'lat' & 'lon'.");

        }

        // Close connection
        httpExchange.close();
    }
}
