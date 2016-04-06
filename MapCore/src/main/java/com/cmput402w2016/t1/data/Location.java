package com.cmput402w2016.t1.data;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;

/**
 * Location should not be called directly and should only be referenced within the Node class
 */
public class Location {
    private String geohash;

    /**
     * Construct a location with a given lat and lon
     *
     * @param lat Double representation of the lat
     * @param lon Double representation of the lon
     */
    public Location(Double lat, Double lon) {
        this.geohash = GeoHash.encodeHash(lat, lon);
    }

    /**
     * Construct a location with a given lat and lon
     *
     * @param lat String representation of the lat
     * @param lon String representation of the lon
     */
    Location(String lat, String lon) {
        this(Double.parseDouble(lat), Double.parseDouble(lon));
    }

    /**
     * Construct a location with a given geohash
     *
     * @param geohash String representation of the geohash
     */
    public Location(String geohash) {
        GeoHash.decodeHash(geohash);
        this.geohash = geohash;
    }

    /**
     * Get the double value of the location's latitude
     *
     * @return Double value of the latitude
     */
    public Double getLat() {
        return GeoHash.decodeHash(this.geohash).getLat();
    }

    /**
     * Set the latitude value of this location
     *
     * @param lat Double value of the latitude
     */
    void setLat(Double lat) {
        LatLong ll = GeoHash.decodeHash(this.geohash);
        this.geohash = GeoHash.encodeHash(lat, ll.getLon());
    }

    /**
     * Get the double value of the location's longitude
     *
     * @return Double value of the longitude
     */
    public Double getLon() {
        return GeoHash.decodeHash(this.geohash).getLon();
    }

    /**
     * Set the longitude value of this location
     *
     * @param lon Double value of the longitude
     */
    void setLon(Double lon) {
        LatLong ll = GeoHash.decodeHash(this.geohash);
        this.geohash = GeoHash.encodeHash(ll.getLat(), lon);
    }

    /**
     * Custom equality method for this location
     *
     * @param object Other object to compare to
     * @return Boolean, true if equals, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof Location)) return false;
        Location loc = (Location) object;
        if (this.geohash == null ? loc.geohash != null : !this.geohash.equals(loc.geohash)) return false;
        return true;
    }

    /**
     * Custom hash code method for this location
     *
     * @return String hashcode of the geohash
     */
    @Override
    public int hashCode() {
        return this.geohash.hashCode();
    }

    /**
     * Compute the geohash from the lat lon, return the string value of the geohash with maximum precision.
     *
     * @return String geohash value
     */
    public String getGeohash() {
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

    /**
     * Return this object's geohash as the string representation of the obj
     *
     * @return String, geohash
     */
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
    public double distance(Location to) {
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

    /**
     * Adapted from http://stackoverflow.com/questions/9457988/bearing-from-one-coordinate-to-another
     * Returns bearing in reference from this point to the given point
     *
     * @param to Second location
     * @return Double, bearing to a given location
     */
    public double bearingTo(Location to) {
        double longitude1 = this.getLon();
        double longitude2 = to.getLon();
        double latitude1 = Math.toRadians(this.getLat());
        double latitude2 = Math.toRadians(to.getLat());
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
        return Math.atan2(y, x);
    }
}
