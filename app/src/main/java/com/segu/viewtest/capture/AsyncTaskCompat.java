package com.segu.viewtest.capture;

import android.os.AsyncTask;
import android.os.Build;

public class AsyncTaskCompat {
    public AsyncTaskCompat() {
    }

    public static <Params, Progress, Result> AsyncTask<Params, Progress, Result> executeParallel(AsyncTask<Params, Progress, Result> task, Params... params) {
        if (task == null) {
            throw new IllegalArgumentException("task can not be null");
        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                AsyncTaskCompatHoneycomb.executeParallel(task, params);
            } else {
                task.execute(params);
            }

            return task;
        }
    }
}