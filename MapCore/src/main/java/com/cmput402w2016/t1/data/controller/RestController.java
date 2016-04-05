package com.cmput402w2016.t1.data.controller;

import com.cmput402w2016.t1.data.Location;
import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;
import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Interacts with our database through REST.
 * Important: Do not use this controller inside webapi! The webapi implements the functionality to support
 * this controller!
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
        setup(restHost);
    }

    private void setup(String restHost) {
        this.client = new HttpClient();
        this.gson = new Gson();
        this.uri = restHost.concat(":").concat(String.valueOf(DEFAULT_REST_PORT));
    }

    public void postTraffic(TrafficData traffic) {
        try {
            // Set SimulatorDataModel
            PostMethod trafficPost = new PostMethod(uri.concat("/traffic"));

            String content = traffic.to_serialized_json();
            StringRequestEntity entity = new StringRequestEntity(content, "application/json", "UTF-8");
            trafficPost.setRequestEntity(entity);

            // Send SimulatorDataModel
            client.executeMethod(trafficPost);

            String responseBodyAsString = trafficPost.getResponseBodyAsString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Node getClosestNode(Location location) {
        try {
            GetMethod nodeGet = new GetMethod(uri.concat(String.format("/node?geohash=%s", location.getGeohash())));
            client.executeMethod(nodeGet);

            String responseBodyAsString = nodeGet.getResponseBodyAsString();
            NodeResponse nodeResponse = gson.fromJson(responseBodyAsString, NodeResponse.class);

            return new Node(nodeResponse.geohash, nodeResponse.tags);

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public SegmentResponse getSegment(Location location) {
        try {
            GetMethod nodeGet = new GetMethod(uri.concat(String.format("/segment?geohash=%s", location.getGeohash())));
            client.executeMethod(nodeGet);

            String responseBodyAsString = nodeGet.getResponseBodyAsString();
            return new SegmentResponse(responseBodyAsString);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
