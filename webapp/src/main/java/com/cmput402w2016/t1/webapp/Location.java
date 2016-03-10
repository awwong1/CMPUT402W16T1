package com.cmput402w2016.t1.webapp;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

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

    public boolean isValid() {
        return this.lat != null && this.lon != null &&
                this.lat > -90.0 && this.lat < 90.0 && this.lon > -180.0 && this.lon < 180.0;
    }

    @Override
    public String toString() {
        return this.lat + ", " + this.lon;
    }
}
