package com.kristenwong.localizationanddailypath;

import java.util.UUID;

/**
 * Created by kristenwong on 11/16/17.
 */

public class SavedLocation {
    private UUID uuid;
    private String name;
    private double latitude, longitude;
    private String address;

    public SavedLocation(UUID id, String n, double lat, double lon, String addr) {
        uuid = id;
        name = n;
        latitude = lat;
        longitude = lon;
        address = addr;
    }

    public SavedLocation(){ uuid = UUID.randomUUID();}

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
}
