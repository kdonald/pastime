package com.pastime.util;

public final class Location {

    private final Double latitude;
    
    private final Double longitude;

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String toString() {
        return getLatitude().toString() + "," + getLongitude().toString();
    }
    
}
