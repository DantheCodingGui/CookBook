package com.danthecodinggui.recipes.view.Loaders;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

public abstract class UpdatingAsyncTaskLoader extends AsyncTaskLoader {

    private int loaderId;

    Handler uiThread;
    private ProgressUpdateListener progressCallback;

    public UpdatingAsyncTaskLoader(Context context, Handler uiThread,
                                   ProgressUpdateListener progressCallback, int loaderId) {
        super(context);
        this.uiThread = uiThread;
        this.progressCallback = progressCallback;
        this.loaderId = loaderId;
    }

    public <T> void UpdateProgress(final T updateValue) {
        uiThread.post(new Runnable() {
            @Override
            public void run() {
                progressCallback.onProgressUpdate(loaderId, updateValue);
            }
        });
    }

    public interface ProgressUpdateListener {
        <T> void onProgressUpdate(int loaderId, T updateValue);
    }
}
