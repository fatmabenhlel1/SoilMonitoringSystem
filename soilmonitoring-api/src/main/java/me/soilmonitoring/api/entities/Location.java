package me.soilmonitoring.api.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

@Embeddable
public class Location {
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String address;

    public Location() {
    }

    public Location(Double latitude, Double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
