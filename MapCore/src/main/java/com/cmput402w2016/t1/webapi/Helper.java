package com.cmput402w2016.t1.webapi;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for all helper methods used by the Web API.
 */
public class Helper {
    /**
     * Change the GET web query to a hashmap of key value pairs
     * Feed the function a raw query string and it will generate a map with key-value pairs to use
     * The following code is adapted from:
     * http://stackoverflow.com/questions/11640025/java-httpserver-httpexchange-get
     *
     * @param query Raw String query grabbed from the httpexchange object
     * @return Hashmap of all string key value pairs split by '=' and separated by '&'
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

    /**
     * Take the Http Exchange object, set the body to be a json object containing the error message with the
     * given response code
     *
     * @param http         HttpExchange object
     * @param responseCode Integer response code to associate with the response
     * @param hint         Error message as a string
     */
    public static void malformedRequestResponse(HttpExchange http, int responseCode, String hint) {
        String response;
        if (hint != null) {
            response = "{\"error\": \"Could not serve request (" + hint + ")\"}";
        } else {
            response = "{\"error\": \"Could not serve request\"}";
        }
        requestResponse(http, responseCode, response);
    }

    /**
     * Take the HttpExchange object, set the body to be the raw string with the given response code
     *
     * @param http         HttpExchange object
     * @param responseCode Integer response code to associate with the response
     * @param msg          Message as a string
     */
    public static void requestResponse(HttpExchange http, int responseCode, String msg) {
        try {
            http.sendResponseHeaders(responseCode, msg.getBytes().length);
            OutputStream os = http.getResponseBody();
            os.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
