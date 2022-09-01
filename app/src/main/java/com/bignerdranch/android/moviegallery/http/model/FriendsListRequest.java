package com.bignerdranch.android.moviegallery.http.model;

import javax.validation.constraints.NotNull;

public class FriendsListRequest extends BasePageRequest{

    @NotNull
    private Integer uid;


    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }
}
