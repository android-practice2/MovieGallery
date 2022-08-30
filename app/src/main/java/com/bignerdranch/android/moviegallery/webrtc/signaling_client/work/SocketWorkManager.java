package com.bignerdranch.android.moviegallery.webrtc.signaling_client.work;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bignerdranch.android.moviegallery.constants.Constants;

import java.util.concurrent.TimeUnit;

public class SocketWorkManager {

    private static final SocketWorkManager instance = new SocketWorkManager();

    public static SocketWorkManager getInstance() {
        return instance;
    }

    public void start(Context context) {
        int uid = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(Constants.PF_UID, -1);
        if (uid < 0) {
            Log.e(SocketWorkManager.class.getSimpleName(), "uid_is_null: uid should not be null, SocketConnectivityWork should be after login");
            return;
        }
        WorkManager workManager = WorkManager.getInstance(context);
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(
                SocketConnectivityWork.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS
        );
        builder.setInitialDelay(0, TimeUnit.MILLISECONDS);

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putInt(Constants.EXTRA_UID, uid);
        builder.setInputData(dataBuilder.build());

        workManager.enqueue(builder.build());


    }

}
