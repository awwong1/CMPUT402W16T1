package com.cmput402w2016.t1.webapp;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.Filter;

import java.util.Arrays;

public class Location {
    private Double lat;
    private Double lon;

    Location(String lat, String lon) {
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    public String computeGeohash() {
        LatLong latLon = new LatLong(lat, lon);
        return GeoHash.encodeHash(latLon);
    }

    private String shorten(String str) {
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length()-1);
        }
        if (str == null) {
            return "";
        }
        return str;
    }

    public String getClosestNode() {
        String geoHash = computeGeohash();
        while (!geoHash.equals("")) {
            System.out.println(geoHash);
            Scan scan = new Scan();
            scan.setRowPrefixFilter(geoHash.getBytes());
            System.out.println("Initialized scanner");
            try {
                System.out.println("Initialized result scanner & scanning");
                ResultScanner rs = App.node_table.getScanner(scan);
                System.out.println("Scan done, getting next result");
                Result result = rs.next();
                if (result == null) {
                    System.out.println("Could not get result");
                } else {
                    System.out.println("Got result");
                    return Arrays.toString(result.getRow());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            geoHash = shorten(geoHash);
        }
        return null;
    }

    public boolean isValid() {
        return this.lat != null && this.lon != null &&
                this.lat > -90.0 && this.lat < 90.0 && this.lon > -180.0 && this.lon < 180.0;
    }

    @Override
    public String toString() {
        return this.lat + ", " + this.lon;
    }
}
