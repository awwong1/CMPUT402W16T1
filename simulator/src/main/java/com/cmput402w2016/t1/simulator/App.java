package com.cmput402w2016.t1.simulator;

import com.cmput402w2016.t1.data.*;
import com.google.gson.Gson;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class App
{
    public static void main(String[] args)
    {
        Gson gson = new Gson();
        File folder = new File("data");
        ArrayList<Data> allData = new ArrayList<Data>();

        // Read & Parse all the data we have
        for(File file : folder.listFiles()) {
            try {
                FileReader reader = new FileReader(file);
                Data data = gson.fromJson(reader, Data.class);
                allData.add(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Infinite Loops of Simulation on the data!
        DatabaseController controller = new RESTController(gson);
        while(true) {
            DateTime now = DateTime.now();
//            // Sleep until we're at the new hour
//            DateTime until = now.plusHours(1).minusMinutes(now.getMinuteOfHour());
//            try {
//                long sleepTime = until.getMillis() - now.getMillis();
//                System.out.println("Sleeping for this many seconds: " + sleepTime/1000);
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            // It's not actually 'now', 'now' was an hour ago...
            for(Data data : allData) {
                // Get traffic sample
                int cars_per_hour;
                try {
                    cars_per_hour = data.generateSampleCarsPerHour(now.getMillis());
                } catch (Exception e) {
                    // Couldn't generate the data for this hour
                    e.printStackTrace();
                    continue;
                }

                // Construct Traffic Object
                // TODO: This is painful, we should have a constructor...
                Traffic traffic = new Traffic();
                traffic.setFrom(data.getFrom());
                traffic.setTo(data.getTo());
                traffic.setKey("CARS_PER_HOUR");
                traffic.setValue((double) cars_per_hour);

                // Post traffic to REST API
                controller.PostTraffic(traffic);
            }
        }


    }
}