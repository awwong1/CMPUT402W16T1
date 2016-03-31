package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.data.Segment;
import com.cmput402w2016.t1.webapi.Helper;
import com.cmput402w2016.t1.webapi.WebApi;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class SegmentHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // Get & parse query
        try {
            String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                String query = httpExchange.getRequestURI().getRawQuery();
                Map<String, String> stringStringMap = Helper.queryToMap(query);
                if (stringStringMap.containsKey("geohash")) {
                    String geohash = stringStringMap.get("geohash");
                    String neighbors = Segment.getClosestSegmentFromGeohash(geohash, WebApi.get_segment_table());
                    Helper.requestResponse(httpExchange, 200, neighbors);
                    httpExchange.close();
                    return;
                }
            }
            Helper.malformedRequestResponse(httpExchange, 400, "Invalid query to the segment api");
            httpExchange.close();
        } catch (Exception e) {
            // Wasn't returned earlier, something must be wrong
            e.printStackTrace();
            Helper.malformedRequestResponse(httpExchange, 400, e.getMessage());
            httpExchange.close();
        }
    }
}
