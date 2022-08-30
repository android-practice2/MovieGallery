package com.bignerdranch.android.moviegallery;

import android.content.Context;

import com.bignerdranch.android.moviegallery.mqtt.MqttWorkManager;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.work.SocketWorkManager;

public class LivenessRegister {
    private static LivenessRegister instance = new LivenessRegister();

    public static LivenessRegister getInstance() {
        return instance;
    }

    public void start(Context context) {
        SocketWorkManager.getInstance().start(context);
        MqttWorkManager.getInstance().start(context);

    }
}
