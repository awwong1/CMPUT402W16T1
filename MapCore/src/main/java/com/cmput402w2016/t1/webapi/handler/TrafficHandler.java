package com.cmput402w2016.t1.webapi.handler;

import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;
import com.cmput402w2016.t1.webapi.Helper;
import com.cmput402w2016.t1.webapi.WebApi;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class TrafficHandler implements HttpHandler {
    public TrafficHandler() {
    }

    /**
     * Take the httpExchange object and grab all of the post data as a String
     *
     * @param httpExchange Object to read the post data from
     * @return String representation of the post data
     */
    private String read_post_body(HttpExchange httpExchange) {
        String rawContent = "";
        InputStreamReader instream = new InputStreamReader(httpExchange.getRequestBody());
        BufferedReader buffer = new BufferedReader(instream);
        String line;
        try {
            while ((line = buffer.readLine()) != null) {
                rawContent += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawContent;
    }

    /**
     * Handle the httpExchange request to the server
     *
     * @param httpExchange object containing the request, http methods, http body
     */
    @Override
    public void handle(HttpExchange httpExchange) {
        // GET, PUT, POST, DELETE
        // Currently only POST is supported
        try {
            String requestMethod = httpExchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("POST")) {
                String rawContent = read_post_body(httpExchange);
                if (rawContent.equals("")) {
                    // POST data missing, can't create blank traffic information
                    Helper.malformedRequestResponse(httpExchange, 400, "POST contained no data");
                    httpExchange.close();
                    return;
                }
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(rawContent);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                TrafficData trafficData = TrafficData.json_to_traffic_data(jsonObject);

                if (trafficData == null || !trafficData.isValid()) {
                    Helper.malformedRequestResponse(httpExchange, 400, "Invalid traffic data posted");
                    httpExchange.close();
                    return;
                }

                // Get the source node.
                Node from_node = Node.getClosestNodeFromLocation(trafficData.getFrom(), WebApi.get_node_table());
                String from_hash = from_node.computeGeohash();

                // Get the closet neighbor geohash.
                String to_hash = from_node.getClosestNeighborGeohash(WebApi.get_segment_table(), trafficData.getTo());

                String long_val = String.valueOf(trafficData.getTimestamp());
                String key_val = trafficData.getKey();
                Double val_val = trafficData.getValue();

                // Put the value into HBase
                long get_last_hour = trafficData.getTimestamp() - (trafficData.getTimestamp() % 3600);
                Put p = new Put(Bytes.toBytes(from_hash + "_" + to_hash + "_" + String.valueOf(get_last_hour)));
                p.addColumn(Bytes.toBytes("data"),
                        Bytes.toBytes(key_val + "~" + long_val),
                        Bytes.toBytes(String.valueOf(val_val)));
                WebApi.get_traffic_table().put(p);

                // This takes the the from: and to: keys and nests the location within, not matching our api
                // Helper.requestResponse(httpExchange, 201, new Gson().toJson(trafficData, TrafficData.class));
                String serialized_json = trafficData.to_serialized_json();
                // Send to Kafka
                send_to_kafka(serialized_json);

                Helper.requestResponse(httpExchange, 201, serialized_json);
                httpExchange.close();
                return;
            } else if (requestMethod.equalsIgnoreCase("GET")) {
                // TODO implement GET
                Helper.malformedRequestResponse(httpExchange, 500, "Traffic GET currently not implemented");
                httpExchange.close();
                return;
            }
            // Submitted a method other than POST
            Helper.malformedRequestResponse(httpExchange, 400, "Invalid query to the traffic api");
            httpExchange.close();

        } catch (Exception e) {
            Helper.malformedRequestResponse(httpExchange, 400, e.getMessage());
            httpExchange.close();
        }
    }

    private void send_to_kafka(String serialized_json) {
        KafkaProducer<String, String> producer;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("acks", "all");
        properties.put("retries", "0");
        properties.put("retries", "0");
        properties.put("batch.size", "16384");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("linger.ms", "0");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("block.on.buffer.full", "true");
        producer = new KafkaProducer<>(properties);
        ProducerRecord<String, String> pr = new ProducerRecord<>("traffic", "data", serialized_json);
        producer.send(pr);
        producer.close();

    }
}