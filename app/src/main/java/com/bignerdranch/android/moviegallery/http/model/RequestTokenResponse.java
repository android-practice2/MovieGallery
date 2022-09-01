package com.bignerdranch.android.moviegallery.http.model;


public class RequestTokenResponse {

    private String putUrl;
    private String getUrl;


    public String getPutUrl() {
        return putUrl;
    }

    public void setPutUrl(String putUrl) {
        this.putUrl = putUrl;
    }

    public String getGetUrl() {
        return getUrl;
    }

    public void setGetUrl(String getUrl) {
        this.getUrl = getUrl;
    }

    @Override
    public String toString() {
        return "{" +
                "putUrl='" + putUrl + '\'' +
                ", getUrl='" + getUrl + '\'' +
                '}';
    }
}
