package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.webapi.Helper;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RootHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map<String, String> message = new HashMap<>();
        message.put("message",
                "Server is up. API reference at " +
                        "https://github.com/cmput402w2016/CMPUT402W16T1/wiki/REST-API-Documentation");
        Helper.requestResponse(httpExchange, 200, new Gson().toJson(message));
        httpExchange.close();
    }
}
