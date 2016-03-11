package com.cmput402w2016.t1.data;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RESTController implements DatabaseController {
    public static final String TRAFFIC_URI = "http://199.116.235.225:8000/traffic";
    private Gson gson;
    private HttpClient client;
    private PostMethod trafficPost;

    public RESTController(Gson gson) {
        this.client = new HttpClient();
        this.gson = gson;
        this.trafficPost = new PostMethod(TRAFFIC_URI);
    }

    public void PostTraffic(Traffic traffic) {
        try {
            // Set data
            StringRequestEntity entity = new StringRequestEntity(gson.toJson(traffic), "application/json", "UTF-8");
            trafficPost.setRequestEntity(entity);

            // Send data
            client.executeMethod(trafficPost);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
