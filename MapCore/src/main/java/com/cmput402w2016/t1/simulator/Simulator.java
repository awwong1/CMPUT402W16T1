package com.cmput402w2016.t1.simulator;

import com.cmput402w2016.t1.data.Traffic;
import com.google.gson.Gson;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public  class Simulator {
    public static void run(String... args) {
        // TODO: Sanity checks
        String hostURI = args[0];

        Gson gson = new Gson();
        File folder = new File("data");
        ArrayList<Data> allData = new ArrayList<>();

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
        DatabaseController controller = new RestController(gson, hostURI);
        while(true) {
            // Start of hour
            DateTime this_hour = DateTime.now().hourOfDay().roundFloorCopy();

            // Sleep until we're at the new hour
            try {
                long sleepTime = DateTime.now().hourOfDay().roundCeilingCopy().getMillis() - this_hour.getMillis();
                System.out.println("Sleeping for this many seconds: " + sleepTime/1000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Simulate everything we have data for
            for(Data data : allData) {
                try {
                    // Get traffic sample (It's not actually 'now' anymore, 'now' was an hour ago)
                    Traffic traffic = data.generateSampleTraffic(this_hour.getMillis());
                    // Post
                    controller.postTraffic(traffic);
                } catch (Exception e) {
                    // Couldn't generate the data for this hour
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
}
