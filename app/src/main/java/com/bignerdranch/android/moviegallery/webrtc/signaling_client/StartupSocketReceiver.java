package com.bignerdranch.android.moviegallery.webrtc.signaling_client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bignerdranch.android.moviegallery.webrtc.signaling_client.work.SocketWorkManager;

public class StartupSocketReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SocketWorkManager.getInstance().startSocketConnectivityWork(context);

    }
}
