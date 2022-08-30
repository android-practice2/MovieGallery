package com.bignerdranch.android.moviegallery.mqtt;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.work.SocketWorkManager;

public class MqttWorkManager {
    private static MqttWorkManager instance = new MqttWorkManager();

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
        final OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(MqttWork.class);
        Data inputData = new Data.Builder()
                .putInt(Constants.EXTRA_UID, uid)
                .build();
        builder.setInputData(inputData);
        final OneTimeWorkRequest request = builder.build();
        workManager.enqueue(request);

    }
}
