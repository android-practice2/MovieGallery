package com.bignerdranch.android.moviegallery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application implements Configuration.Provider {

    private static final Map<String, Object> data = new HashMap<>();

    @Inject
    HiltWorkerFactory mHiltWorkerFactory;


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
