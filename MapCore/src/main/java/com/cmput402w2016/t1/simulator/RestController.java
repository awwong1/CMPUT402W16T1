package com.cmput402w2016.t1.simulator;

import com.cmput402w2016.t1.data.Traffic;
import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Interacts with our database through REST
 */
public class RestController implements DatabaseController {
    /**
     * Endpoint for our REST Server
     */
    public static final Integer DEFAULT_REST_PORT = 8000;
    /**
     * Constructed string for sending traffic requests
     */
    private String uri;
    private Gson gson;
    private HttpClient client;

    /**
     * @param restHost IP or URL where the REST server is running
     */
    public RestController(String restHost) {
        setup(new Gson(), restHost);
    }

    /**
     * @param gson GSON instance
     * @param restHost IP or URL where the REST server is running
     */
    public RestController(Gson gson, String restHost) {
        setup(gson, restHost);
    }

    public void setup(Gson gson, String restHost) {
        this.client = new HttpClient();
        this.gson = new Gson();
        this.uri = restHost.concat(":").concat(String.valueOf(DEFAULT_REST_PORT));
    }

    public void postTraffic(Traffic traffic) {
        try {
            // Set Data
            PostMethod trafficPost = new PostMethod(uri.concat("/traffic"));
            StringRequestEntity entity = new StringRequestEntity(gson.toJson(traffic), "application/json", "UTF-8");
            trafficPost.setRequestEntity(entity);

            // Send Data
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
