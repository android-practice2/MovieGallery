package com.bignerdranch.android.moviegallery.webrtc.signaling_client.model;


public class SignalingMessage {
    private String type;
    private String room;
    private Object content;//json, transparent.  gson deser as com.google.gson.internal.LinkedTreeMap

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "SignalingMessage{" +
                "type='" + type + '\'' +
                ", room='" + room + '\'' +
                ", content=" + content +
                '}';
    }
}
