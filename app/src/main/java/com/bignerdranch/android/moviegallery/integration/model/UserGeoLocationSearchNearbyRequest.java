package com.bignerdranch.android.moviegallery.integration.model;

import javax.validation.constraints.NotNull;


public class UserGeoLocationSearchNearbyRequest extends BasePageRequest{

    @NotNull
    private Float latitude;
    @NotNull
    private Float longitude;


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

    @Override
    public String toString() {
        return "{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
