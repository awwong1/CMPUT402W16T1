package com.cmput402w2016.t1.webapp.handler;

import com.cmput402w2016.t1.webapp.GetHelpers;
import com.cmput402w2016.t1.webapp.Location;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TrafficHandler implements HttpHandler {
    Gson gson;

    public TrafficHandler(Gson gson) {
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
