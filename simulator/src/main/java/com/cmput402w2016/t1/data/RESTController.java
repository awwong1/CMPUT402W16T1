package com.cmput402w2016.t1.data;

import com.google.gson.Gson;
import org.apache.commons.httpclient.methods.PostMethod;

public class RESTController implements DatabaseController {
    private Gson gson;

    public RESTController(Gson gson) {
        this.gson = gson;
    }

    public void PostTraffic(Traffic traffic) {
        PostMethod post = new PostMethod("http://199.116.235.225:8000/traffic");
        String object = gson.toJson(traffic);
        post.setRequestBody(object);

    }
}
