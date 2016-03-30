package com.cmput402w2016.t1.webapi;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    /*
     * The following code is adapted from:
     * http://stackoverflow.com/questions/11640025/java-httpserver-httpexchange-get
     * Feed the function a raw query string and it will generate a map with key-value pairs to use
     */
    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    public static void malformedRequestResponse(HttpExchange http, int responseCode, String hint) {
        String response;
        if (hint != null) {
            response = "{\"error\": \"Could not serve request (" + hint + ")\"}";
        } else {
            response = "{\"error\": \"Could not serve request\"}";
        }
        requestResponse(http, responseCode, response);
    }

    public static void requestResponse(HttpExchange http, int responseCode, String msg) {
        System.out.println(msg);
        try {
            http.sendResponseHeaders(responseCode, msg.getBytes().length);
            OutputStream os = http.getResponseBody();
            os.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
