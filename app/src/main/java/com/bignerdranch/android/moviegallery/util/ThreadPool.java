package com.bignerdranch.android.moviegallery.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private static final ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    public static ExecutorService getExecutorService() {
        return mExecutorService;
    }
}
