package com.bignerdranch.android.moviegallery.integration.model;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotEmpty;


public class User implements Serializable {
    private Integer uid;

    @NotEmpty
    private String nickname;

    @NotEmpty
    private String phone_number;

    private String avatar;



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
                ", nickname='" + nickname + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) && Objects.equals(nickname, user.nickname) && Objects.equals(phone_number, user.phone_number) && Objects.equals(avatar, user.avatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, nickname, phone_number, avatar);
    }
}
