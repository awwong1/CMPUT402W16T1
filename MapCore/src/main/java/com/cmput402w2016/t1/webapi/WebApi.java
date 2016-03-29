package com.cmput402w2016.t1.webapi;

import com.cmput402w2016.t1.util.Util;
import com.cmput402w2016.t1.webapi.handler.NodeHandler;
import com.cmput402w2016.t1.webapi.handler.TrafficHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebApi {

    private static Table node_table = null;
    private static Table segment_table = null;
    private static Table traffic_table = null;

    public static void start_web_api(String raw_port) {
        if (!StringUtils.isNumeric(raw_port)) {
            System.err.println(raw_port + " is not a valid port integer.");
            return;
        }

        System.out.println("Initializing HBase tables");
        node_table = Util.get_table("node");
        segment_table = Util.get_table("segment");
        traffic_table = Util.get_table("traffic");
        if (node_table == null || segment_table == null || traffic_table == null) {
            System.err.println("One or more required tables does not exist!");
            return;
        }

        // Start up the HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(Integer.valueOf(raw_port)), 0);
            server.createContext("/node", new NodeHandler());
            server.createContext("/traffic", new TrafficHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server has started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Table get_node_table() {
        return WebApi.node_table;
    }

    public static Table get_segment_table() {
        return WebApi.segment_table;
    }

    public static Table get_traffic_table() {
        return WebApi.traffic_table;
    }
}
