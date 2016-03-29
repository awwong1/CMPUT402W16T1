package com.cmput402w2016.t1.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang.math.NumberUtils;

public class TrafficData {
    private Location from;
    private Location to;
    // This is supposed to be an epoch time (seconds since epoch), not java's milliseconds
    private long timestamp;
    private String key;
    private String value;

    public TrafficData(Location from, Location to, long timestamp, String key, String value) {
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof TrafficData)) return false;
        TrafficData td = (TrafficData) object;
        if (!this.from.equals(td.from)) return false;
        if (!this.to.equals(td.to)) return false;
        if (this.timestamp != td.timestamp) return false;
        if (this.key == null ? td.key != null : !this.key.equals(td.key)) return false;
        if (this.value == null ? td.value != null : !this.value.equals(td.value)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.from.hashCode() + this.to.hashCode() + (this.key + this.value).hashCode();
    }

    public boolean isValid() {
        // TODO, no timestamps greater than one hour in the future (maybe?)
        return from.isValid() && to.isValid() && timestamp > 0 && NumberUtils.isNumber(value) && !key.contains("~");
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    /**
     * Given a raw json object, return a traffic data object
     *
     * @param raw_json_object Raw JSON object containing all traffic data information
     * @return TrafficData object matching the raw_json_object
     */
    public static TrafficData json_to_traffic_data(JsonObject raw_json_object) {
        JsonObject raw_from = raw_json_object.get("from").getAsJsonObject();
        JsonObject raw_to = raw_json_object.get("to").getAsJsonObject();
        JsonElement raw_timestamp = raw_json_object.get("time");
        JsonElement raw_key = raw_json_object.get("key");
        JsonElement raw_value = raw_json_object.get("value");

        Location from = null;
        Location to = null;
        try {
            JsonElement from_lat = raw_from.get("lat");
            JsonElement from_lon = raw_from.get("lon");
            JsonElement to_lat = raw_to.get("lat");
            JsonElement to_lon = raw_to.get("lon");
            from = new Location(from_lat.getAsString(), from_lon.getAsString());
            to = new Location(to_lat.getAsString(), to_lon.getAsString());
        } catch (Exception ignored) {
            // This is fine, lat and lon don't have to be provided as we also accept geohashes
        }
        try {
            String from_geohash = raw_from.getAsString();
            String to_geohash = raw_to.getAsString();
            from = new Location(from_geohash);
            to = new Location(to_geohash);
        } catch (Exception ignored) {
            // This is fine, perhaps from and to were set before
        }
        if (from == null || to == null) {
            // guess not, return null lol
            return null;
        }
        long timestamp = raw_timestamp.getAsLong();
        String key = raw_key.getAsString();
        String value = raw_value.getAsString();

        return new TrafficData(from, to, timestamp, key, value);
    }
}