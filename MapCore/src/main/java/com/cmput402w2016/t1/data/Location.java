package com.cmput402w2016.t1.data;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

/**
 * Location should not be called directly and should only be referenced within the Node class
 */
class Location {
    private String geohash;

    Location(Double lat, Double lon) {
        this.geohash = GeoHash.encodeHash(lat, lon);
    }

    Location(String lat, String lon) {
        this(Double.parseDouble(lat), Double.parseDouble(lon));
    }

    Location(String geohash) {
        GeoHash.decodeHash(geohash);
        this.geohash = geohash;
    }

    Double getLat() {
        return GeoHash.decodeHash(this.geohash).getLat();
    }

    Double getLon() {
        return GeoHash.decodeHash(this.geohash).getLon();
    }

    void setLat(Double lat) {
        LatLong ll = GeoHash.decodeHash(this.geohash);
        this.geohash = GeoHash.encodeHash(lat, ll.getLon());
    }

    void setLon(Double lon) {
        LatLong ll = GeoHash.decodeHash(this.geohash);
        this.geohash = GeoHash.encodeHash(ll.getLat(), lon);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof Location)) return false;
        Location loc = (Location) object;
        if (this.geohash == null ? loc.geohash != null : !this.geohash.equals(loc.geohash)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.geohash.hashCode();
    }

    /**
     * Compute the geohash from the lat lon, return the string value of the geohash with maximum precision.
     *
     * @return String geohash value
     */
    String getGeohash() {
        return this.geohash;
    }

    /**
     * Check if the lat lon provided are actual, valid lat lon values.
     *
     * @return true if location is valid, false otherwise
     */
    boolean isValid() {
        try {
            GeoHash.decodeHash(this.geohash);
            return true;
        } catch (Exception ignored) {

        }
        return false;
    }

    @Override
    public String toString() {
        return this.geohash;
    }

    /**
     * Distance calculation of lat and lon taken from
     * http://stackoverflow.com/a/5396425
     *
     * @param to Location to point
     * @return double, distance between two points in meters
     */
    double distance(Location to) {
        LatLong ll = GeoHash.decodeHash(this.geohash);
        LatLong tll = GeoHash.decodeHash(to.geohash);
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = tll.getLat() - ll.getLat();
        double deltaLon = tll.getLon() - ll.getLon();
        double angle = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2) +
                        Math.cos(ll.getLat()) * Math.cos(tll.getLat()) *
                                Math.pow(Math.sin(deltaLon / 2), 2)));
        return radius * angle;
    }
}