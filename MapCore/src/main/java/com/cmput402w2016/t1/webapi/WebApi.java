package com.cmput402w2016.t1.webapi;

import com.cmput402w2016.t1.util.Util;
import com.cmput402w2016.t1.webapi.handler.NodeHandler;
import com.cmput402w2016.t1.webapi.handler.RootHandler;
import com.cmput402w2016.t1.webapi.handler.SegmentHandler;
import com.cmput402w2016.t1.webapi.handler.TrafficHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Class for handling the web api interface. Will start up the http server and serve requests
 */
public class WebApi {

    private static Table node_table = null;
    private static Table segment_table = null;
    private static Table traffic_table = null;

    /**
     * Start up all the required HBase tables and the http web server
     *
     * @param raw_port port number as a string to serve underneath
     */
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
            server.createContext("/segment", new SegmentHandler());
            server.createContext("/", new RootHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server has started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the initialized static node table singleton
     *
     * @return HBase table of nodes
     */
    public static Table get_node_table() {
        return WebApi.node_table;
    }

    /**
     * Get the initialized static segment table singleton
     *
     * @return HBase table of segments
     */
    public static Table get_segment_table() {
        return WebApi.segment_table;
    }

    /**
     * Get the initialized static traffic table singleton
     *
     * @return HBase table of traffic
     */
    public static Table get_traffic_table() {
        return WebApi.traffic_table;
    }
}
