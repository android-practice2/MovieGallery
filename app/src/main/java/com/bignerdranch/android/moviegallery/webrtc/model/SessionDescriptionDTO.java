package com.bignerdranch.android.moviegallery.webrtc.model;

import org.webrtc.SessionDescription;

public class SessionDescriptionDTO {
    public SessionDescription.Type type;
    public String description;

    @Override
    public String toString() {
        return "SessionDescriptionDTO{" +
                "type=" + type +
                ", description='" + description + '\'' +
                '}';
    }
}
