package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.webapi.Helper;
import com.cmput402w2016.t1.webapi.WebApi;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class NodeHandler implements HttpHandler {
    public NodeHandler() {
    }

    private void setNode(Node node, HttpExchange httpExchange) {
        String serialized_json = node.toSerializedJson();
        Helper.requestResponse(httpExchange, 200, serialized_json);
        httpExchange.close();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        // Get & parse query
        try {
            String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                String query = httpExchange.getRequestURI().getRawQuery();
                Map<String, String> stringStringMap = Helper.queryToMap(query);
                if (stringStringMap.containsKey("id")) {
                    String osmId = stringStringMap.get("id");
                    Node node = Node.getNodeFromID(osmId, WebApi.get_node_table());
                    if (node != null) {
                        setNode(node, httpExchange);
                        return;
                    }
                    Helper.malformedRequestResponse(httpExchange, 404, "No node matches provided osm id");
                    httpExchange.close();
                    return;
                } else if (stringStringMap.containsKey("geohash")) {
                    String geohash = stringStringMap.get("geohash");
                    Node node = Node.getClosestNodeFromGeohash(geohash, WebApi.get_node_table());
                    if (node != null) {
                        setNode(node, httpExchange);
                        return;
                    }
                    Helper.malformedRequestResponse(httpExchange, 404, "No node remotely matches provided geohash");
                    httpExchange.close();
                    return;
                } else if (stringStringMap.containsKey("lat") && stringStringMap.containsKey("lon")) {
                    String latitude = stringStringMap.get("lat");
                    String longitude = stringStringMap.get("lon");
                    Node node = Node.getClosestNodeFromLatLon(latitude, longitude, WebApi.get_node_table());
                    if (node != null) {
                        setNode(node, httpExchange);
                        return;
                    }
                    Helper.malformedRequestResponse(httpExchange, 404, "No node remotely matches provided lat & lon");
                    httpExchange.close();
                    return;
                }
            }
            Helper.malformedRequestResponse(httpExchange, 400, "Invalid query to the node api");
            httpExchange.close();
        } catch (Exception e) {
            // Wasn't returned earlier, something must be wrong
            e.printStackTrace();
            Helper.malformedRequestResponse(httpExchange, 400, e.getMessage());
            httpExchange.close();
        }
    }
}
