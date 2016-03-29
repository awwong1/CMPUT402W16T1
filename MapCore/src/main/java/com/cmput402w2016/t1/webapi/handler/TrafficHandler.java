package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.data.TrafficData;
import com.cmput402w2016.t1.webapi.Helper;
import com.cmput402w2016.t1.webapi.WebApi;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TrafficHandler implements HttpHandler {
    Gson gson;

    public TrafficHandler(Gson gson) {
        this.gson = gson;
    }

    public void handle(HttpExchange httpExchange) {
        try {
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

                if (rawContent.equals("")) {
                    // POST data missing, can't create blank traffic information
                    Helper.malformedRequestResponse(httpExchange, 400, "POST contained no data");
                    httpExchange.close();
                    return;
                }

                TrafficData trafficData = null;
                try {
                    trafficData = gson.fromJson(rawContent, TrafficData.class);
                } catch (Exception ignored) {
                }

                if (trafficData != null) {
                    if (!trafficData.isValid()) {
                        Helper.malformedRequestResponse(httpExchange, 400, "Invalid traffic data posted");
                        httpExchange.close();
                        return;
                    }
                    String from_hash = trafficData.getFrom().getClosestNodeGeohash(WebApi.node_table);
                    String to_hash = trafficData.getTo().getClosestNodeGeohash(WebApi.node_table);
                    String long_val = String.valueOf(trafficData.getTimestamp());
                    String key_val = trafficData.getKey();
                    String val_val = trafficData.getValue();

                    Put p = new Put(Bytes.toBytes(from_hash + "_" + to_hash));
                    p.addColumn(Bytes.toBytes("data"), Bytes.toBytes(key_val + "~" + long_val), Bytes.toBytes(val_val));
                    try {
                        WebApi.traffic_table.put(p);
                        // TODO: This should return the key instead of the lat/lon?
                        Helper.requestResponse(httpExchange, 201, gson.toJson(trafficData, TrafficData.class));
                        httpExchange.close();
                        return;
                    } catch (IOException e) {
                        Helper.malformedRequestResponse(httpExchange, 400, "Could not put traffic data in HBase");
                        httpExchange.close();
                        return;
                    }
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
        } catch (Exception e) {
            Helper.malformedRequestResponse(httpExchange, 400, e.getStackTrace().toString());
            httpExchange.close();
            return;
        }
        //httpExchange.close();
    }
}