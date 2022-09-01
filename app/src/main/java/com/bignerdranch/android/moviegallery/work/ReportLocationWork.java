package com.bignerdranch.android.moviegallery.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bignerdranch.android.moviegallery.http.AppClient;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class ReportLocationWork extends Worker {
    private static final String TAG = "ReportLocationWork";
    private final AppClient mAppClient;
    @AssistedInject
    public ReportLocationWork(@Assisted @NonNull Context context,
                              @Assisted @NonNull WorkerParameters workerParams,
                              AppClient appClient) {
        super(context, workerParams);
        mAppClient = appClient;
    }


    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork");

        //get location

        //report location
        // TODO: 2022/8/3

        return null;
    }

}
