package com.cmput402w2016.t1.data;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

/**
 * Location should not be called directly and should only be referenced within the Node class
 */
class Location {
    private Double lat;
    private Double lon;

    Location(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    Location(String lat, String lon) {
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    Location(String geohash) {
        LatLong latlong = GeoHash.decodeHash(geohash);
        this.lat = latlong.getLat();
        this.lon = latlong.getLon();
    }

    Double getLat() {
        return this.lat;
    }

    Double getLon() {
        return this.lon;
    }

    void setLat(Double lat) {
        this.lat = lat;
    }

    void setLon(Double lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof Location)) return false;
        Location loc = (Location) object;
        if (Double.compare(this.lat, loc.lat) != 0) return false;
        if (Double.compare(this.lon, loc.lon) != 0) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.lat.hashCode() + this.lon.hashCode();
    }

    /**
     * Compute the geohash from the lat lon, return the string value of the geohash with maximum precision.
     *
     * @return String geohash value
     */
    String computeGeohash() {
        return GeoHash.encodeHash(lat, lon);
    }

    /**
     * Check if the lat lon provided are actual, valid lat lon values.
     *
     * @return true if location is valid, false otherwise
     */
    boolean isValid() {
        return this.lat != null && this.lon != null &&
                this.lat > -90.0 && this.lat < 90.0 && this.lon > -180.0 && this.lon < 180.0;
    }

    @Override
    public String toString() {
        return this.lat + ", " + this.lon;
    }

    /**
     * Distance calculation of lat and lon taken from
     * http://stackoverflow.com/a/5396425
     *
     * @param from Location from point
     * @param to   Location to point
     * @return double, distance between two points in meters
     */
    static double distance(Location from, Location to) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = to.lat - from.lat;
        double deltaLon = to.lon - from.lon;
        double angle = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2) +
                        Math.cos(from.lat) * Math.cos(to.lat) *
                                Math.pow(Math.sin(deltaLon / 2), 2)));
        return radius * angle;
    }
}