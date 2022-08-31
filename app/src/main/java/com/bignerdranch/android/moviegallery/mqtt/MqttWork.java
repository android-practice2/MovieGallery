package com.bignerdranch.android.moviegallery.mqtt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bignerdranch.android.moviegallery.constants.Constants;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class MqttWork extends Worker {
    private final int uid;
    private final Context context;
    private final AppMqttClient mAppMqttClient;

    @AssistedInject
    public MqttWork(@Assisted @NonNull Context context,
                    @Assisted @NonNull WorkerParameters workerParams,
                    AppMqttClient appMqttClient) {
        super(context, workerParams);
        this.context = context;
        this.uid = workerParams.getInputData().getInt(Constants.EXTRA_UID, -1);

        mAppMqttClient = appMqttClient;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(getClass().getSimpleName(), "doWork");
        mAppMqttClient.start(uid);

        return Result.success();


    }


}
