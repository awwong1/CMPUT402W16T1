package com.cmput402w2016.t1.data;

import com.github.davidmoten.geo.GeoHash;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

public class Location {
    private Double lat;
    private Double lon;

    Location(String lat, String lon) {
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    /**
     * Compute the geohash from the lat lon, return the string value of the geohash with maximum precision.
     *
     * @return String geohash value
     */
    public String computeGeohash() {
        return GeoHash.encodeHash(lat, lon);
    }

    /**
     * Take the string, shorten it by one character, return the string.
     * If the string is the empty string "", return null.
     *
     * @param str String to shorten
     * @return Shortened string or null
     */
    private String shorten(String str) {
        if ("".equals(str)) {
            return null;
        }
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        if (str == null) {
            return "";
        }
        return str;
    }

    /**
     * Find the closest node geohash to this location. Requires access to the HBase node table.
     *
     * @param node_table HBase table containing all of the nodes neighbors
     * @return String representation of the node in geohash
     */
    public String getClosestNodeGeohash(Table node_table) {
        String geoHash = computeGeohash();
        while (geoHash != null) {
            Scan scan = new Scan();
            scan.setRowPrefixFilter(geoHash.getBytes());
            try {
                ResultScanner rs = node_table.getScanner(scan);
                Result result = rs.next();
                if (result != null) {
                    return new String(result.getRow());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            geoHash = shorten(geoHash);
        }
        return "";
    }

    /**
     * Check if the lat lon provided are actual, valid lat lon values.
     *
     * @return true if location is valid, false otherwise
     */
    public boolean isValid() {
        return this.lat != null && this.lon != null &&
                this.lat > -90.0 && this.lat < 90.0 && this.lon > -180.0 && this.lon < 180.0;
    }

    @Override
    public String toString() {
        return this.lat + ", " + this.lon;
    }
}