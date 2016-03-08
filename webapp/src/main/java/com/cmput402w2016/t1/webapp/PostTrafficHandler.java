package com.cmput402w2016.t1.webapp;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by kent on 08/03/16.
 */
public class PostTrafficHandler implements HttpHandler {
    Gson gson;

    PostTrafficHandler(Gson gson) {
        this.gson = gson;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
        TrafficData trafficData = gson.fromJson(reader, TrafficData.class);

        if(trafficData != null) {
            // TODO: Put information in HBase
            // DEBUG
            System.out.println("Location Data");
            System.out.println("  from: " + trafficData.from);
            System.out.println("  to: " + trafficData.to);
            System.out.println("  key: " + trafficData.key);
            System.out.println("  value: " + trafficData.value);
        } else {
            // TODO: Error, couldn't parse data.
            GetHelpers.malformedRequestResponse(httpExchange, "Couldn't parse traffic data JSON");
        }

        httpExchange.close();
    }

    private class TrafficData {
        private Location from;
        private Location to;
        private String key;
        private String value;
    }
}
