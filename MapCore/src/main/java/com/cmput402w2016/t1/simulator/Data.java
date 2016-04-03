package com.cmput402w2016.t1.simulator;

import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.TrafficData;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;


/**
 * Holds and represents the models and data that we're simulating data for, in reference to a segment
 */
public class Data {
    private Map<String, Integer> traffic;
    private transient Map<Integer, NormalDistribution> carsPerHour;
    private String source;
    private Node from;
    private Node to;

    public Data(String source, Node from, Node to) {
        this.source = source;
        this.from = from;
        this.to = to;
        traffic = new HashMap<>();
    }

    public void generateModel() {
        generateCarsPerHourModel();
    }

    /**
     * Cars Per Hour model works by creating 24 normal distributions (1 for each hour of the day).
     *  After the model is generated, you can sample it with the sampleCarsPerHourModel function
     */
    public void generateCarsPerHourModel() {
        // Get statistical data for counts at each hour
        Map<Integer, SummaryStatistics> funstuff = new HashMap<Integer, SummaryStatistics>();
        for (Map.Entry<String, Integer> count : traffic.entrySet()) {
            // Java stores times in milliseconds since epoch, hence the *1000.
            String key = count.getKey();
            System.out.println("Key: " + key);
            long timestamp = Long.parseLong(key, 10) * 1000;
            int cars_per_hour = count.getValue();

            DateTime time = new DateTime(timestamp, DateTimeZone.UTC);
            int hour = time.getHourOfDay();

            SummaryStatistics stats;
            if (funstuff.containsKey(hour)) {
                // Started gathering statistics, add to it
                stats = funstuff.get(hour);
            } else {
                // Will now start gathering statistics
                stats = new SummaryStatistics();
                funstuff.put(hour, stats);
            }

            stats.addValue(cars_per_hour);
        }

        // Make model based on statistics gathered
        carsPerHour = new HashMap<Integer, NormalDistribution>();
        for (Map.Entry<Integer, SummaryStatistics> per_hour_stats : funstuff.entrySet()) {
            int hour = per_hour_stats.getKey();
            SummaryStatistics stats = per_hour_stats.getValue();

            // Create distribution based on stats
            try {
                NormalDistribution distribution = new NormalDistribution(stats.getMean(), stats.getStandardDeviation());
                carsPerHour.put(hour, distribution);
                System.out.println("Made a distribution for hour " + hour + "!");
            } catch (Exception e) {
                System.out.println(":( No distribution made for hour " + hour);
            }
        }
    }

    /**
     * Samples a data point from the Cars Per Hour model, using the given timestamp
     *
     * @param timestamp Timestamp to simulate traffic data for. Specify in milliseconds since epoch. Use UTC!
     * @return Simulated data for Cars Per Hour based on the model
     * @throws Exception
     */
    public TrafficData sampleCarsPerHourModel(long timestamp) throws Exception {
        // Verify we have a traffic model for this segment
        if (carsPerHour == null) {
            generateModel();
        }

        DateTime time = new DateTime(timestamp, DateTimeZone.UTC);
        int hour = time.getHourOfDay();
        TrafficData traffic = null;

        // Add cars-per-hour data
        if (carsPerHour.containsKey(hour)) {
            // We have traffic data for the hour, sample it.
            Double sample = carsPerHour.get(hour).sample(1)[0];
            if (sample <= 0) {
                traffic = new TrafficData(from.getLocation(), to.getLocation(), time.getMillis(), "CARS_PER_HOUR", 0.0);
            } else {
                traffic = new TrafficData(from.getLocation(), to.getLocation(), time.getMillis(), "CARS_PER_HOUR", sample);
            }
        } else {
            // TODO: See if we have the previous hour and the next hour and interpolate the data if we do
        }

        if (traffic != null && traffic.isValid()) {
            // Traffic has data
            return traffic;
        } else {
            // Traffic didn't have any data
            throw new Exception(String.format("No traffic model for the given timestamp: %d (Hour: %d)", time.getMillis(), time.getHourOfDay()));
        }
    }
}