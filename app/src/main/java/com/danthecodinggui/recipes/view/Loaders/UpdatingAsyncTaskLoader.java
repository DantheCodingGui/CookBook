package com.danthecodinggui.recipes.view.Loaders;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Extension to AsyncTaskLoader adding the ability to periodically update UI whilst loading data
 */
public abstract class UpdatingAsyncTaskLoader<T> extends AsyncTaskLoader<T> {

    private int loaderId;

    Handler uiThread;
    private ProgressUpdateListener progressCallback;

    UpdatingAsyncTaskLoader(Context context, Handler uiThread,
                                   ProgressUpdateListener progressCallback, int loaderId) {
        super(context);
        this.uiThread = uiThread;
        this.progressCallback = progressCallback;
        this.loaderId = loaderId;
    }

    /**
     * Updates the linked view with partially loaded values
     * @param updateValue Object being loaded
     */
    <T> void UpdateProgress(final T updateValue) {
        uiThread.post(new Runnable() {
            @Override
            public void run() {
                progressCallback.onProgressUpdate(loaderId, updateValue);
            }
        });
    }

    public interface ProgressUpdateListener {
        /**
         * Called partially through loading to update UI with some of the loaded values
         * @param loaderId The id associated to the loader on creation
         * @param updateValue The partially loaded object/object list being passed to the view
         */
        <T> void onProgressUpdate(int loaderId, T updateValue);
    }
}
