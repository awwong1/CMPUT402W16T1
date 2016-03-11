package com.cmput402w2016.t1.webapp;

import com.cmput402w2016.t1.webapp.handler.NodeHandler;
import com.cmput402w2016.t1.webapp.handler.TrafficHandler;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    protected static Table node_table = null;
    protected static Table segment_table = null;
    protected static Table traffic_table = null;

    public static void main(String[] args) {
        // This is how much code it takes if you do it the "Proper" way, without using the deprecated methods

        try {
            Configuration hbconf;
            Connection conn;
            Admin hba;
            TableName[] tableNames;
            Table[] tables ;

            // Create Configuration from HBaseConfiguration
            hbconf = HBaseConfiguration.create();
            // Create the Connection from the Configuration
            conn = ConnectionFactory.createConnection(hbconf);
            // Get the Admin object from the Connection
            hba = conn.getAdmin();
            // Get the table names
            tableNames = hba.listTableNames();

            tables = new Table[tableNames.length];
            int tableIndex = 0;
            for (TableName tableName : tableNames) {
                Table table;
                table = conn.getTable(tableName);
                tables[tableIndex] = table;
                tableIndex += 1;
            }
            for (Table table : tables) {
                String tableName = table.getName().getQualifierAsString();
                switch (tableName) {
                    case "node":
                        node_table = table;
                        break;
                    case "segment":
                        segment_table = table;
                        break;
                    case "traffic":
                        traffic_table = table;
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (node_table == null || segment_table == null || traffic_table == null) {
            System.err.println("One or more required tables does not exist!");
            System.exit(1);
        }

        // Start up the HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            Gson gson = new Gson();

            // Get Handlers
            server.createContext("/node", new NodeHandler(gson));

            // Post Handlers
            server.createContext("/traffic", new TrafficHandler(gson));

            // Start Server
            server.setExecutor(null);
            server.start();
            System.out.println("Server has started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
