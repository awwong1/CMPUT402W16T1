package com.cmput402w2016.t1.simulator;

import com.cmput402w2016.t1.data.Data;
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

        for(File file : folder.listFiles()) {
            try {
                FileReader reader = new FileReader(file);
                Data data = gson.fromJson(reader, Data.class);
                allData.add(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        for(Data data : allData) {
            System.out.println(data.getFrom() + " to " + data.getTo());
            System.out.println("# of Data Points: " + data.traffic.size());

            // ATTEMPT STATS, LOL
            System.out.println("lol, stats?");
            data.generateModel();
            long time = DateTime.now().getMillis();
            try {
                System.out.println("How many cars do we estimate will be passing through this intersection this hour?");
                for(int i = 0; i < 100; i++) {
                    System.out.println("  This many: " + data.generateSampleCarsPerHour(time));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}