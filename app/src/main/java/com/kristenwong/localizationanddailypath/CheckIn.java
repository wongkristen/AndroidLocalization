package com.kristenwong.localizationanddailypath;

import java.util.UUID;

/**
 * Created by kristenwong on 11/15/17.
 */

public class CheckIn {
    private String name;
    private double latitude, longitude;
    private String address;
    private String time;
    private UUID uuid;

    public CheckIn(String n, double lat, double lon, String addr, String t) {
        name = n;
        latitude = lat;
        longitude = lon;
        address = addr;
        time = t;
        uuid = UUID.randomUUID();
    }

    public CheckIn(UUID id) {
        uuid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public UUID getUuid() {
        return uuid;
    }

}
