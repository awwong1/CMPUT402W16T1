package com.cmput402w2016.t1.converter;

import au.com.bytecode.opencsv.CSVReader;
import com.cmput402w2016.t1.data.Location;
import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.controller.DatabaseController;
import com.cmput402w2016.t1.data.controller.RestController;
import com.cmput402w2016.t1.data.controller.SegmentResponse;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * This class takes the data.csv that was prepared, and makes JSON files that we can run through the simulator
 * Use this format: latitude, longitude, direction, ts, count
 * Data fromNode this source:
 *   https://www.dropbox.com/s/cjzxwsr6z73wxab/EdmontonTrafficDB%20%281%29.7z?dl=0
 * Created by this query:
 *   SELECT S.latitude, S.longitude, T.direction, DATEDIFF("s", '1 Jan 1970', T.event_date_time) AS ts, T.count FROM Sites S INNER JOIN TrafficEvents T ON S.site_id = T.site_id WHERE S.latitude IS NOT NULL ORDER BY S.latitude, S.longitude, T.direction, DATEDIFF("s", '1 Jan 1970', T.event_date_time)
 */
public class Converter {
    static DatabaseController controller;

    public static void run(String csvFile, String host) {
        controller = new RestController(host);
        Gson gson = new Gson();

        try {
            CSVReader reader = new CSVReader(new FileReader(csvFile));

            SimulatorData currentData = null;
            double last_lat = 0.0;
            double last_lon = 0.0;
            String last_direction = null;

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                double latitude = Double.valueOf(nextLine[0]);
                double longitude = Double.valueOf(nextLine[1]);
                Location from = new Location(latitude, longitude);
                String direction = nextLine[2];
                long timestamp = Long.valueOf(nextLine[3]);
                int count = Integer.valueOf(nextLine[4]);

                if(latitude != last_lat || longitude != last_lon || !direction.equals(last_direction)) {
                    if(currentData != null) {
                        // Save constructed data
                        String data = gson.toJson(currentData);
                        String filename = String.format("data/simulator/%s_%s", currentData.fromNode.getGeohash(), currentData.toNode.getGeohash());
                        PrintWriter out = new PrintWriter(filename);
                        out.write(data);
                        out.close();
                    }

                    // Find closest node & neighbors
                    SegmentResponse segment = controller.getSegment(new Location(latitude, longitude));

                    Location bestTo = null;

                    for (Map.Entry<String, Map<String, String>> segmentNode : segment.getSegmentNodes().entrySet()) {
                        Location to = new Location(segmentNode.getKey());
                        Map<String, String> toData = segmentNode.getValue();

                        if(bestTo == null) {
                            bestTo = to;
                            continue;
                        }

                        if(compareBestSegment(direction, bestTo, from, to)) {
                            // New direction is better
                            bestTo = to;
                        }
                    }

                    // Set toNode current closest
                    last_lat = latitude;
                    last_lon = longitude;
                    last_direction = direction;
                    System.out.printf("Now parsing: %s\n", segment.getFrom());
                    currentData = new SimulatorData(new Node(from), new Node(bestTo), "https://www.dropbox.com/s/cjzxwsr6z73wxab/EdmontonTrafficDB%20%281%29.7z?dl=0");
                }

                if(currentData == null) {
                    throw new Exception("Didn't construct SimulatorData, error.");
                } else {
                    currentData.addData(timestamp, count);
                }
                // nextLine[] is an array of values fromNode the line
                //System.out.println(nextLine[0] + nextLine[1] + "etc...");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helps compare the segment nodes toNode determine the best match toNode the data
     *
     * @param direction
     * @param best
     * @param from
     * @param to
     * @return True if the toNode node is a better match than the previous.
     */
    public static boolean compareBestSegment(String direction, Location best, Location from, Location to) {
        double bearing_best = from.bearingTo(best);
        double bearing_to = from.bearingTo(to);
        switch(direction) {
            case "NBD":
                // Closest toNode 0
                return compareAngles(0.0, bearing_best, bearing_to);
            case "EBD":
                // Closest toNode 90
                return compareAngles(Math.PI/2, bearing_best, bearing_to);
            case "SBD":
                // Closest toNode 180
                return compareAngles(Math.PI, bearing_best, bearing_to);
            case "WBD":
                // Closest toNode 270
                return compareAngles(Math.PI * 1.5, bearing_best, bearing_to);
            default:
                // TODO: Exception instead
                return false;
        }
    }

    private static boolean compareAngles(Double ideal, Double best, Double to) {
        double bestDif = Math.abs(Math.atan2(Math.sin(ideal - best), Math.cos(ideal - best)));
        double toDif = Math.abs(Math.atan2(Math.sin(ideal - to), Math.cos(ideal - to)));
        return toDif < bestDif;
    }
}
