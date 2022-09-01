package com.bignerdranch.android.moviegallery.http.model;


import java.util.Objects;

public class UserLocationProjection {
    private Integer uid;

    private String nickname;
    private String phone_number;
    private String avatar;

    private Float latitude;
    private Float longitude;
    private Float distance;


    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
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

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLocationProjection that = (UserLocationProjection) o;
        return Objects.equals(uid, that.uid) && Objects.equals(nickname, that.nickname) && Objects.equals(phone_number, that.phone_number) && Objects.equals(avatar, that.avatar) && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && Objects.equals(distance, that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, nickname, phone_number, avatar, latitude, longitude, distance);
    }
}
