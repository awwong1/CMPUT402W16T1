package com.cmput402w2016.t1.webapp;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args)
    {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            Gson gson = new Gson();

            // Get Handlers
            server.createContext("/get/node", new GetNodeHandler(gson));

            // Post Handlers
            server.createContext("/post/traffic", new PostTrafficHandler(gson));

            // Start Server
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Hello World!");
        System.out.println("What is this magic. It still continues doing stuff?");
    }
}
