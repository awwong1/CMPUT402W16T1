package com.cmput402w2016.t1.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TrafficData {
    private Location from = null;
    private Location to = null;
    // This is supposed to be an epoch time (seconds since epoch), not java's milliseconds
    private Long timestamp = null;
    private String key = null;
    private Double value = null;

    public TrafficData(Location from, Location to, long timestamp, String key, Double value) {
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
        if (Long.compare(this.timestamp, td.timestamp) != 0) return false;
        if (this.key == null ? td.key != null : !this.key.equals(td.key)) return false;
        if (Double.compare(this.value, td.value) != 0) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.from.hashCode() + this.to.hashCode() + (this.key + this.value).hashCode();
    }

    public boolean isValid() {
        boolean not_null = from != null && to != null && timestamp != null && key != null && value != null;
        if (!not_null) {
            return false;
        }
        boolean from_valid = from.isValid();
        boolean to_valid = to.isValid();
        long curr_epoch_seconds = System.currentTimeMillis() / 1000L;
        boolean timestamp_valid = this.getTimestamp() < (curr_epoch_seconds + 3600);
        boolean key_valid = !key.contains("~");
        return from_valid && to_valid && timestamp_valid && key_valid;
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

    public Double getValue() {
        return value;
    }

    /**
     * Given a raw json object, return a traffic data object
     *
     * @param raw_json_object Raw JSON object containing all traffic data information
     * @return TrafficData object matching the raw_json_object
     */
    public static TrafficData json_to_traffic_data(JsonObject raw_json_object) {
        JsonElement raw_from = raw_json_object.get("from");
        JsonElement raw_to = raw_json_object.get("to");
        JsonElement raw_timestamp = raw_json_object.get("timestamp");
        JsonElement raw_key = raw_json_object.get("key");
        JsonElement raw_value = raw_json_object.get("value");

        Location from = null;
        Location to = null;
        try {
            JsonElement from_lat = raw_from.getAsJsonObject().get("lat");
            JsonElement from_lon = raw_from.getAsJsonObject().get("lon");
            JsonElement to_lat = raw_to.getAsJsonObject().get("lat");
            JsonElement to_lon = raw_to.getAsJsonObject().get("lon");
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
        Double value = raw_value.getAsDouble();

        return new TrafficData(from, to, timestamp, key, value);
    }

    public String to_serialized_json() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("from", this.from.getGeohash());
        jsonObject.addProperty("to", this.to.getGeohash());
        jsonObject.addProperty("timestamp", this.getTimestamp());
        jsonObject.addProperty("key", this.getKey());
        jsonObject.addProperty("value", this.getValue());
        return jsonObject.toString();
    }
}