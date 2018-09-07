package com.danthecodinggui.recipes.view.Loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

public class GetMethodStepsLoader extends UpdatingAsyncTaskLoader {

    ContentResolver contentResolver;

    private List<String> methodSteps;

    private long recipePk;

    public GetMethodStepsLoader(Context context, Handler uiThread, ProgressUpdateListener progressCallback, int loaderId, long recipePk) {
        super(context, uiThread, progressCallback, loaderId);

        contentResolver = context.getContentResolver();
        this.recipePk = recipePk;
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (methodSteps != null) {
            Log.v(DATA_LOADING, "Method Steps loading started: Using cached values");
            deliverResult(methodSteps);
        }
        else {
            Log.v(DATA_LOADING, "Method Steps loading started: Load new values");
            methodSteps = new ArrayList<>();
            forceLoad();
        }
    }

    @Nullable
    @Override
    public Object loadInBackground() {
        String[] projection = {
                ProviderContract.MethodStepEntry.TEXT
        };

        String[] arguments = { Long.toString(recipePk) };

        Cursor cursor = contentResolver.query(
                ProviderContract.METHOD_URI,
                projection,
                ProviderContract.METHOD_SELECTION,
                arguments,
                ProviderContract.METHOD_SORT_ORDER
        );

        int recordsGathered = 0;

        while (cursor.moveToNext()) {
            methodSteps.add(cursor.getString(
                    cursor.getColumnIndexOrThrow(ProviderContract.MethodStepEntry.TEXT)));


            recordsGathered = methodSteps.size();
            if (recordsGathered % 10 == 0)
                UpdateProgress(methodSteps.subList(recordsGathered - 10, recordsGathered - 1));
        }
        cursor.close();

        return methodSteps.subList(recordsGathered - (recordsGathered % 10), recordsGathered);
    }
}
