package com.bignerdranch.android.moviegallery.integration.model;


public class UserGetDetailV2Response {
//========= User fields
    private Integer uid;

    private String nickname;

    private String avatar;

    private String phone_number;

//========== friend fields
    private Boolean areFriend;


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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Boolean getAreFriend() {
        return areFriend;
    }

    public void setAreFriend(Boolean areFriend) {
        this.areFriend = areFriend;
    }
}
