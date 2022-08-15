package com.bignerdranch.android.moviegallery.integration.model;


import javax.validation.constraints.NotEmpty;

public class UserRegisterRequest {


    @NotEmpty
    private String nickname;

    private String avatar;

    @NotEmpty
    private String phone_number;

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

    @Override
    public String toString() {
        return "{" +
                "nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", phone_number='" + phone_number + '\'' +
                '}';
    }
}
