package com.cmput402w2016.t1.webapp;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.Filter;

import java.io.IOException;
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
        return str;
    }

    public String getClosestNode() {
        String geoHash = computeGeohash();
        while (!geoHash.equals("")) {
            System.out.println(geoHash);
            Scan scan = new Scan();
            Filter filter = new ColumnPrefixFilter(geoHash.getBytes());
            scan.setFilter(filter);
            try {
                ResultScanner rs = App.node_table.getScanner(scan);
                Result result = rs.next();
                return Arrays.toString(result.getRow());
            } catch (IOException e) {
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
