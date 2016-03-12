package com.cmput402w2016.t1.data;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;

public class Data {
    // Description where the data is sourced from
    String source;
    // Node which the traffic is coming from
    Location from;
    // Node which the traffic is heading towards
    Location to;
    // Epoch Timestamp -> Count
    // TODO: Temporary public for testing.
    public Map<String, Integer> traffic;
    // Hour of the day -> Normal Distribution
    public transient Map<Integer, NormalDistribution> traffic_model;

    Data() {
        traffic = new HashMap<String, Integer>();
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void generateModel() {
        // Get statistical data for counts at each hour
        Map<Integer, SummaryStatistics> funstuff = new HashMap<Integer, SummaryStatistics>();
        for (Map.Entry<String, Integer> count: traffic.entrySet()) {
            // Java stores times in milliseconds since epoch, hence the *1000.
            String key = count.getKey();
            System.out.println("Key: " + key);
            long timestamp = Long.parseLong(key, 10) * 1000;
            int cars_per_hour = count.getValue();

            DateTime time = new DateTime(timestamp, DateTimeZone.UTC);
            int hour = time.getHourOfDay();

            SummaryStatistics stats;
            if(funstuff.containsKey(hour)) {
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
        traffic_model = new HashMap<Integer, NormalDistribution>();
        for (Map.Entry<Integer, SummaryStatistics> per_hour_stats : funstuff.entrySet()) {
            int hour = per_hour_stats.getKey();
            SummaryStatistics stats = per_hour_stats.getValue();

            // Create distribution based on stats
            try {
                NormalDistribution distribution = new NormalDistribution(stats.getMean(), stats.getStandardDeviation());
                traffic_model.put(hour, distribution);
                System.out.println("Made a distribution for hour " + hour + "!");
            } catch(Exception e) {
                System.out.println(":( No distribution made for hour " + hour);
            }
        }
    }

    // Gets traffic count based on the current time
    // Timestamp in java time (milliseconds since epoch)
    // Use UTC/GMT timezone!
    public int generateSampleCarsPerHour(long timestamp) throws Exception {
        // Verify we have a traffic model for this segment
        if(traffic_model == null) {
            generateModel();
        }

        DateTime time = new DateTime(timestamp, DateTimeZone.UTC);
        int hour = time.getHourOfDay();

        if(traffic_model.containsKey(hour)) {
            Double sample = traffic_model.get(hour).sample(1)[0];
            if(sample <= 0) {
                return 0;
            } else {
                return sample.intValue();
            }
        } else {
            // TODO: See if we have the previous hour and the next hour.
        }
        // TODO: A better exception.
        // This exception means we didn't have enough data to generate a sample data point.
        // i.e. we didn't have > 2 counts for the current hour.
        throw new Exception();
    }
}
