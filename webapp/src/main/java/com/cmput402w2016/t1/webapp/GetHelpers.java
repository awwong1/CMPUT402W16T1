package com.cmput402w2016.t1.webapp;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kent on 08/03/16.
 */
public class GetHelpers {
    /*
      The following code is adapted from:
        http://stackoverflow.com/questions/11640025/java-httpserver-httpexchange-get
      Feed the function a raw query string and it will generate a map with key-value pairs to use
     */
    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
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

    public static void malformedRequestResponse(HttpExchange http, String hint) throws IOException {
        String response;
        if(hint != null) {
            response = "{\"error\": \"Could not serve request (" + hint + ")\"}";
        } else {
            response = "{\"error\": \"Could not serve request\"}";
        }
        http.sendResponseHeaders(400, response.length());
        OutputStream os = http.getResponseBody();
        os.write(response.getBytes());
    }
}
