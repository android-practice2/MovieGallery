package com.bignerdranch.android.moviegallery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import com.bignerdranch.android.moviegallery.webrtc.signaling_client.work.SocketWorkManager;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application implements Configuration.Provider {

    private static final Map<String, Object> data = new HashMap<>();

    @Inject
    HiltWorkerFactory mHiltWorkerFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        enableSocketLiveness();

        enableJULToSLF4J();

    }

    private void enableSocketLiveness() {
        LivenessRegister.getInstance().start(this);
    }

    private void enableJULToSLF4J() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }


    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .setWorkerFactory(mHiltWorkerFactory)
                .build();
    }

    public static Map<String, Object> getData() {
        return data;
    }
}
