package com.bignerdranch.android.moviegallery.integration.model;



import android.location.Location;

import javax.validation.constraints.NotNull;


public class UserGeoLocationAddLocationRequest {

    @NotNull
    private Integer uid;
    @NotNull
    private Float latitude;
    @NotNull
    private Float longitude;

    public UserGeoLocationAddLocationRequest(Integer uid,  Location location ) {
        this.uid = uid;
        this.latitude =(float) location.getLatitude();
        this.longitude =(float)  location.getLongitude();
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }
}
