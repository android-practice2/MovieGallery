package com.bignerdranch.android.moviegallery.webrtc;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class BaseSdpObserver implements SdpObserver {
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.i(getClass().getSimpleName(), "onCreateSuccess" + " " + sessionDescription);

    }

    @Override
    public void onSetSuccess() {
        Log.i(getClass().getSimpleName(), "onSetSuccess");

    }

    @Override
    public void onCreateFailure(String s) {
        Log.i(getClass().getSimpleName(), "onCreateFailure" + " " + s);

    }

    @Override
    public void onSetFailure(String s) {
        Log.i(getClass().getSimpleName(), "onSetFailure" + " " + s);

    }
}
