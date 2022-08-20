package com.bignerdranch.android.moviegallery.webrtc.model;

public class IceCandidateDTO {
    public String sdpMid;
    public int sdpMLineIndex;
    public String sdp;


    @Override
    public String toString() {
        return "{" +
                "sdpMid='" + sdpMid + '\'' +
                ", sdpMLineIndex=" + sdpMLineIndex +
                ", sdp='" + sdp + '\'' +
                '}';
    }
}
