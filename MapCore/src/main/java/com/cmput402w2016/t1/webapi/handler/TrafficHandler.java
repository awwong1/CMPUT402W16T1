package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;
import com.cmput402w2016.t1.webapi.Helper;
import com.cmput402w2016.t1.webapi.WebApi;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TrafficHandler implements HttpHandler {
    public TrafficHandler() {
    }

    /**
     * Take the httpExchange object and grab all of the post data as a String
     *
     * @param httpExchange Object to read the post data from
     * @return String representation of the post data
     */
    private String read_post_body(HttpExchange httpExchange) {
        String rawContent = "";
        InputStreamReader inStream = new InputStreamReader(httpExchange.getRequestBody());
        BufferedReader buffer = new BufferedReader(inStream);
        String line;
        try {
            while ((line = buffer.readLine()) != null) {
                rawContent += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawContent;
    }

    /**
     * Handle the httpExchange request to the server
     *
     * @param httpExchange object containing the request, http methods, http body
     */
    @Override
    public void handle(HttpExchange httpExchange) {
        // GET, PUT, POST, DELETE
        // Currently only POST is supported
        try {
            String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("POST")) {
                String rawContent = read_post_body(httpExchange);
                if (rawContent.equals("")) {
                    // POST data missing, can't create blank traffic information
                    Helper.malformedRequestResponse(httpExchange, 400, "POST contained no data");
                    httpExchange.close();
                    return;
                }
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(rawContent);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                TrafficData trafficData = TrafficData.json_to_traffic_data(jsonObject);

                if (trafficData == null || !trafficData.isValid()) {
                    Helper.malformedRequestResponse(httpExchange, 400, "Invalid traffic data posted");
                    httpExchange.close();
                    return;
                }

                // Get the source node.
                Node from_node = Node.getClosestNodeFromLocation(trafficData.getFrom().getLocation(), WebApi.get_node_table());
                String from_hash = from_node.getGeohash();

                // Get the closet neighbor geohash
                String to_hash = from_node.getClosestNeighborGeohash(WebApi.get_segment_table(), trafficData.getTo().getLocation());

                String long_val = String.valueOf(trafficData.getTimestamp());
                String key_val = trafficData.getKey();
                Double val_val = trafficData.getValue();

                // Put the value into HBase
                // TODO: Partition based on hour?
                Put p = new Put(Bytes.toBytes(from_hash + "_" + to_hash));
                p.addColumn(Bytes.toBytes("data"),
                        Bytes.toBytes(key_val + "~" + long_val),
                        Bytes.toBytes(String.valueOf(val_val)));
                WebApi.get_traffic_table().put(p);

                // This takes the the from: and to: keys and nests the location within, not matching our api
                // Helper.requestResponse(httpExchange, 201, new Gson().toJson(trafficData, TrafficData.class));
                Helper.requestResponse(httpExchange, 201, trafficData.to_serialized_json());
                httpExchange.close();
                return;
            } else if (requestMethod.equalsIgnoreCase("GET")) {
                // TODO implement GET
                Helper.malformedRequestResponse(httpExchange, 500, "Traffic GET currently not implemented");
                httpExchange.close();
                return;
            }
            // Submitted a method other than POST
            Helper.malformedRequestResponse(httpExchange, 400, "Invalid query to the traffic api");
            httpExchange.close();

        } catch (Exception e) {
            Helper.malformedRequestResponse(httpExchange, 400, e.getMessage());
            httpExchange.close();
        }
    }
}