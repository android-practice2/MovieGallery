package com.bignerdranch.android.moviegallery.integration.model;


public class ChatPostMsg {
    private int uid;
    private int peerUid;

    private String content;


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getPeerUid() {
        return peerUid;
    }

    public void setPeerUid(int peerUid) {
        this.peerUid = peerUid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
