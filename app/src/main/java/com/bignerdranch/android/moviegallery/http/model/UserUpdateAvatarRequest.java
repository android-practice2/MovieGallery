package com.bignerdranch.android.moviegallery.http.model;


import javax.validation.constraints.NotEmpty;

public class UserUpdateAvatarRequest {

    @NotEmpty
    private Integer uid;
    @NotEmpty
    private String avatar;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "{" +
                "uid=" + uid +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
