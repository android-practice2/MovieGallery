package com.bignerdranch.android.moviegallery.webrtc.signaling_client.model;

public class JoinRequest {
    private String room;

    private Integer uid;

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

    @Override
    public String toString() {
        return "JoinRequest{" +
                "room='" + room + '\'' +
                ", uid=" + uid +
                '}';
    }
}
