package com.bignerdranch.android.moviegallery.http.model;

import javax.validation.constraints.NotNull;

public class FriendsAddRequest {

    @NotNull
    private Integer uid;

    @NotNull
    private Integer friend_uid;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getFriend_uid() {
        return friend_uid;
    }

    public void setFriend_uid(Integer friend_uid) {
        this.friend_uid = friend_uid;
    }
}
