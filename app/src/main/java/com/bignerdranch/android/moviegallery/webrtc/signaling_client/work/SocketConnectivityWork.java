package com.bignerdranch.android.moviegallery.webrtc.signaling_client.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.webrtc.signaling_client.SocketClient;

public class SocketConnectivityWork extends Worker {
    private WorkerParameters workerParams;

    private Context mContext;

    public SocketConnectivityWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.workerParams = workerParams;
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(getClass().getSimpleName(), "doWork");
        int uid = workerParams.getInputData().getInt(Constants.EXTRA_UID, -1);
        if (uid > 0) {
            SocketClient.ensureSocket(mContext, uid);
            return Result.success();
        } else {
            Log.e(getClass().getSimpleName(), "uid_is_null before ensureSocket");
            return Result.failure();
        }

    }
}
