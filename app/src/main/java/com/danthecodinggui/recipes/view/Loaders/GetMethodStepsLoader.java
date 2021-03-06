package com.danthecodinggui.recipes.view.Loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.MethodStep;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

public class GetMethodStepsLoader extends AsyncTaskLoader<List<MethodStep>> {

    private ContentResolver contentResolver;

    private List<MethodStep> cachedRecords;

    private long recipePk;

    private ContentObserver methodStepsObserver;

    private Handler uiThread;

    public GetMethodStepsLoader(Context context, Handler uiThread, long recipePk) {
        super(context);

        this.uiThread = uiThread;
        contentResolver = context.getContentResolver();
        this.recipePk = recipePk;
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (cachedRecords != null) {
            Log.v(DATA_LOADING, "Ingredients loading started: Using cached values");
            deliverResult(cachedRecords);
        }
        else {
            Log.v(DATA_LOADING, "Ingredients loading started: Load new values");
            forceLoad();
        }

        if (methodStepsObserver == null) {
            methodStepsObserver = new ContentObserver(uiThread) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    onContentChanged();
                }
            };
            contentResolver.registerContentObserver(ProviderContract.RECIPES_URI, false, methodStepsObserver);
        }
    }

    @Nullable
    @Override
    public List<MethodStep> loadInBackground() {

        List<MethodStep> methodSteps = new ArrayList<>();

        String[] projection = {
                ProviderContract.MethodStepEntry.STEP_TEXT,
                ProviderContract.MethodStepEntry.STEP_NO
        };

        //Link to recipe _id
        String[] arguments = { Long.toString(recipePk) };

        Cursor cursor = contentResolver.query(
                ProviderContract.METHOD_URI,
                projection,
                ProviderContract.METHOD_SELECTION,
                arguments,
                ProviderContract.MethodStepEntry.STEP_NO + " ASC"
        );


        MethodStep temp;

        while (cursor.moveToNext()) {

            temp = new MethodStep(
                    cursor.getString(cursor.getColumnIndexOrThrow(
                        ProviderContract.MethodStepEntry.STEP_TEXT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(
                        ProviderContract.MethodStepEntry.STEP_NO)));

            methodSteps.add(temp);
        }
        cursor.close();

        return methodSteps;
    }

    @Override
    public void deliverResult(List<MethodStep> data) {
        if (isReset()) {
            cachedRecords = null;
            return;
        }

        List<MethodStep> oldCache = cachedRecords;
        cachedRecords = data;

        if (isStarted())
            super.deliverResult(data);

        if (oldCache != null && oldCache != data)
            cachedRecords = null;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (cachedRecords != null)
            cachedRecords = null;

        if (methodStepsObserver != null) {
            contentResolver.unregisterContentObserver(methodStepsObserver);
            methodStepsObserver = null;
        }
    }
}
