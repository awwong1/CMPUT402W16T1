package com.cmput402w2016.t1.data;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * Created by kent on 10/03/16.
 */
public class HBaseController implements DatabaseController {
    HTable trafficTable;

    public HBaseController() {
        Configuration hbconf = HBaseConfiguration.create();
        try {
            trafficTable = new HTable(hbconf, "traffic");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PostTraffic(Traffic traffic) {
        // Key
        String key = traffic.getFrom().computeGeohash() + "_" + traffic.getTo().computeGeohash();
        Put p = new Put(Bytes.toBytes(key));

//        try {
//            trafficTable.put(p);
//        } catch (IOException e) {
//            e.printStackTrace();
//            // TODO: Cache? Try again?
//            // Right now it'll just ignore the input request
//        }
    }
}
