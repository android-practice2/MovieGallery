package com.bignerdranch.android.moviegallery.webrtc.signaling_client.model;


public class JoinServerPush {

    private String room;
    private Integer uid;
    private Integer peerUid;


    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getPeerUid() {
        return peerUid;
    }

    public void setPeerUid(Integer peerUid) {
        this.peerUid = peerUid;
    }

    @Override
    public String toString() {
        return "JoinServerPush{" +
                "room='" + room + '\'' +
                ", uid=" + uid +
                ", peerUid=" + peerUid +
                '}';
    }
}
