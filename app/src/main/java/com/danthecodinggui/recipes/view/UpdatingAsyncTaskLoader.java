package com.danthecodinggui.recipes.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

abstract class UpdatingAsyncTaskLoader extends AsyncTaskLoader {

    private int loaderId;

    private Handler uiThread;
    private ProgressUpdateListener progressCallback;

    UpdatingAsyncTaskLoader(Context context, Handler uiThread,
                                   ProgressUpdateListener progressCallback, int loaderId) {
        super(context);
        this.uiThread = uiThread;
        this.progressCallback = progressCallback;
        this.loaderId = loaderId;
    }

    <T> void UpdateProgress(final T updateValue) {
        uiThread.post(new Runnable() {
            @Override
            public void run() {
                progressCallback.onProgressUpdate(loaderId, updateValue);
            }
        });
    }

    interface ProgressUpdateListener {
        <T> void onProgressUpdate(int loaderId, T updateValue);
    }
}
