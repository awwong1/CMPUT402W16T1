package com.cmput402w2016.t1.data;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

public class Location {
    Double lat;
    Double lon;

    @Override
    public String toString() {
        return "(Lat: " + lat + ", Lon:" + lon + ")";
    }

    public boolean isValid() {
        return this.lat > -90.0 && this.lat < 90.0 && this.lon > -180.0 && this.lon < 180.0;
    }
    public String computeGeohash() {
        LatLong latLon = new LatLong(lat, lon);
        return GeoHash.encodeHash(latLon);
    }
}
