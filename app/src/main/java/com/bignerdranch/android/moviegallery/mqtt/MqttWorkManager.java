package com.bignerdranch.android.moviegallery.mqtt;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.work.SocketWorkManager;

import java.util.concurrent.TimeUnit;

public class MqttWorkManager {
    private static final MqttWorkManager instance = new MqttWorkManager();

    public static MqttWorkManager getInstance() {
        return instance;
    }

    public void start(Context context) {
        int uid = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(Constants.PF_UID, -1);
        if (uid < 0) {
            Log.e(SocketWorkManager.class.getSimpleName(), "uid_is_null: uid should not be null, SocketConnectivityWork should be after login");
            return;
        }

        final WorkManager workManager = WorkManager.getInstance(context);
//        final OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(MqttWork.class);
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(MqttWork.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS
        );
        builder.setInitialDelay(0, TimeUnit.MILLISECONDS);

        Data inputData = new Data.Builder()
                .putInt(Constants.EXTRA_UID, uid)
                .build();
        builder.setInputData(inputData);
        workManager.enqueue(builder.build());

    }
}
