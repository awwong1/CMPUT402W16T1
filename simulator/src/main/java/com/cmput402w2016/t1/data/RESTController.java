package com.cmput402w2016.t1.data;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;

public class RESTController implements DatabaseController {
    private Gson gson;
    private HttpClient client;

    public RESTController(Gson gson) {
        client = new HttpClient();
        this.gson = gson;
    }

    public void PostTraffic(Traffic traffic) {
        // Create post request
        PostMethod post = new PostMethod("http://199.116.235.225:8000/traffic");
        String object = gson.toJson(traffic);
        post.setRequestBody(object);

        try {
            // Send data
            client.executeMethod(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
