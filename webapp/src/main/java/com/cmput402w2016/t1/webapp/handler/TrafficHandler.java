package com.cmput402w2016.t1.webapp.handler;

import com.cmput402w2016.t1.webapp.Helper;
import com.cmput402w2016.t1.webapp.data.TrafficData;
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

    public void handle(HttpExchange httpExchange) {
        // GET, PUT, POST, DELETE
        // Currently only POST is supported
        String requestMethod = httpExchange.getRequestMethod();
        System.out.println(requestMethod);
        if (requestMethod.equalsIgnoreCase("POST")) {
            String rawContent = "";
            try (InputStreamReader instream = new InputStreamReader(httpExchange.getRequestBody());
                 BufferedReader buffer = new BufferedReader(instream)) {
                String line;
                while ((line = buffer.readLine()) != null) {
                    rawContent += line;
                }
                System.out.println(rawContent);
            } catch (Exception e) {
                // POST data read exception, can't post invalid traffic information
                e.printStackTrace();
                Helper.malformedRequestResponse(httpExchange, 400, "Could not read POST data");
                httpExchange.close();
                return;
            }

            System.out.println("Pre POST Contained No Data");
            if (rawContent.equals("")) {
                // POST data missing, can't create blank traffic information
                Helper.malformedRequestResponse(httpExchange, 400, "POST contained no data");
                httpExchange.close();
                return;
            }

            System.out.println("Pre TrafficData Obj Instantiation");
            TrafficData trafficData = null;
            try {
                trafficData = gson.fromJson(rawContent, TrafficData.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Post TrafficData Obj Instantiation");
            if (trafficData != null) {
                if (!trafficData.isValid()) {
                    Helper.malformedRequestResponse(httpExchange, 400, "Invalid traffic data posted");
                    httpExchange.close();
                    return;
                }
                // TODO: Put information in HBase
                // DEBUG
                System.out.println("Getting closest node");
                System.out.println(trafficData.getFrom().getClosestNode());
                System.out.println(trafficData.getTo().getClosestNode());
                System.out.println("Location Data");
                System.out.println("  from: " + trafficData.getFrom());
                System.out.println("  to: " + trafficData.getTo());
                System.out.println("  key: " + trafficData.getKey());
                System.out.println("  value: " + trafficData.getValue());
                Helper.malformedRequestResponse(httpExchange, 400, "DEBUG: dis be ok");
                httpExchange.close();
                return;
            } else {
                // Submitted POST data that did not parse into the Traffic Data object
                Helper.malformedRequestResponse(httpExchange, 400, "Could not parse POST data into traffic data JSON");
                httpExchange.close();
                return;
            }
        } else {
            // Submitted a method other than POST
            Helper.malformedRequestResponse(httpExchange, 400, "Currently only creation supported through POST");
            httpExchange.close();
            return;
        }

        //httpExchange.close();
    }
}
