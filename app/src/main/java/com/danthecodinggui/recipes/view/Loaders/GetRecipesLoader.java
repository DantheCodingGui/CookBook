package com.danthecodinggui.recipes.view.Loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.LogTags;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

public class GetRecipesLoader extends UpdatingAsyncTaskLoader<List<Recipe>>{

    private ContentResolver contentResolver;

    private List<Recipe> cachedRecords;

    private ImagePermissionsListener permissionsCallback;

    private boolean waitingForPermissionResponse = false;

    private ForceLoadContentObserver contentObserver;

    public GetRecipesLoader(Context context, Handler uiThread, ProgressUpdateListener updateCallback,
                     ImagePermissionsListener permissionCallback, int loaderId) {
        super(context, uiThread, updateCallback, loaderId);
        contentResolver = context.getContentResolver();
        this.permissionsCallback = permissionCallback;

        Log.v(DATA_LOADING, "Constructor()");
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (cachedRecords != null) {
            Log.v(DATA_LOADING, "Recipe loading started: Using cached values");
            deliverResult(cachedRecords);
        }
        else if (takeContentChanged() || cachedRecords == null){
            Log.v(DATA_LOADING, "Recipe loading started: Load new values");
            forceLoad();
        }

        if (contentObserver == null) {
            contentObserver = new ForceLoadContentObserver();
            contentResolver.registerContentObserver(ProviderContract.METHOD_URI, false, contentObserver);
        }

        Log.v(DATA_LOADING, "onStartLoading()");
    }

    @Override
    public List<Recipe> loadInBackground() {
        Log.v(DATA_LOADING, "loadInBackground()");

        List<Recipe> records = new ArrayList<>();

        //Query recipes table for all records
        Cursor baseCursor = contentResolver.query(
                ProviderContract.RECIPES_URI,
                ProviderContract.RECIPE_PROJECTION_FULL,
                null,
                null,
                ProviderContract.RecipeEntry.VIEW_ORDER + " ASC"
        );
        //Cursor for the ingredients/method queries afterwards
        Cursor countCursor;

        //Temporary holder variable
        Recipe temp;

        int recordsGathered = 0;

        while (baseCursor.moveToNext()) {
            //Get base recipe data
            temp = BuildBaseModel(baseCursor);

            //Add ingredients data to record
            String[] countSelArgs = { Long.toString(
                    baseCursor.getLong(baseCursor.getColumnIndexOrThrow(
                            ProviderContract.RecipeEntry._ID))) };

            countCursor = contentResolver.query(
                    ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.COUNT_INGREDIENTS_PROJECTION,
                    ProviderContract.INGREDIENTS_SELECTION,
                    countSelArgs,
                    null
            );
            if (countCursor == null)
                Log.e(LogTags.CONTENT_PROVIDER, "Previews Loader: ingredients cursor is null");
            else
                temp = AddIngredientCount(countCursor, temp);

            //Add method data to record
            countCursor = contentResolver.query(
                    ProviderContract.METHOD_URI,
                    ProviderContract.COUNT_STEPS_PROJECTION,
                    ProviderContract.METHOD_SELECTION,
                    countSelArgs,
                    null
            );
            if (countCursor == null)
                Log.e(LogTags.CONTENT_PROVIDER, "Previews Loader: method cursor is null");
            else
                temp = AddStepsCount(countCursor, temp);


            //Need to ask for permission if a recipe includes a photo
            if (temp.hasPhoto() && permissionsCallback != null) {
                AskForReadPermission();


                while (waitingForPermissionResponse)
                    ;//Log.v("busy", "waiting!!");
                Log.v("busy", "---------------------------------------------------------------------------------------------------------!");
            }

            records.add(temp);

            recordsGathered = records.size();

//            //In case there's a vast amount of records, update the ui every 10 loaded records
//            if (recordsGathered % 10 == 0)
//                UpdateProgress(records.subList(recordsGathered - 10, recordsGathered - 1));
        }
        baseCursor.close();

        Log.v(DATA_LOADING, "END OF loadInBackground()");
        //Return remaining records in list
        return records;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
        Log.v(DATA_LOADING, "onStopLoading()");
    }

    @Override
    protected void onReset() {
        Log.v(DATA_LOADING, "onReset()");

        onStopLoading();

        if (cachedRecords != null)
            cachedRecords = null;

        if (contentObserver != null) {
            contentResolver.unregisterContentObserver(contentObserver);
            contentObserver = null;
        }
    }

    @Override
    public void deliverResult(List<Recipe> data) {
        Log.v(DATA_LOADING, "deliverResult()");
        if (isReset()) {
            cachedRecords = null;
            return;
        }

        List<Recipe> oldCache = cachedRecords;
        cachedRecords = data;

        if (isStarted())
            super.deliverResult(data);

        if (oldCache != null && oldCache != data)
            cachedRecords = null;
    }

    /**
     * Ask the calling Activity for the READ_EXTERNAL_STORAGE permission, as one of the loaded records
     * has one
     */
    private void AskForReadPermission() {
        uiThread.post(new Runnable() {
            @Override
            public void run() {
                permissionsCallback.onImagePermRequested();
                //Nullify reference to avoid memory leak
                permissionsCallback = null;
            }
        });
        waitingForPermissionResponse = true;
    }

    /**
     * Called to cancel busy waiting of loader, can now continue loading data
     */
    public void onPermissionResponse() {
        waitingForPermissionResponse = false;
    }

    /**
     * Decides what type of Recipe to build based on what data is saved in the table
     * record
     * @return The base Recipe constructed with the data found in the record
     */
    private Recipe BuildBaseModel(Cursor cursor) {

        //Constant values, always used in constructor
        long pk = cursor.getLong(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry._ID));
        String recipeTitle = cursor.getString(cursor.getColumnIndexOrThrow(
                ProviderContract.RecipeEntry.TITLE));

        int calories = 0;
        int timeInMins = 0;
        String photoPath = null;

        //If optional values exist assign to variables
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.CALORIES_PER_PERSON)))
            calories = cursor.getInt(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.CALORIES_PER_PERSON));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.DURATION)))
            timeInMins = cursor.getInt(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.DURATION));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.IMAGE_PATH)))
            photoPath = cursor.getString(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.IMAGE_PATH));

        return new Recipe.RecipeBuilder(pk, recipeTitle)
                .calories(calories)
                .timeInMins(timeInMins)
                .imageFilePath(photoPath)
                .build();
    }

    /**
     * Add the number of ingredients to the current record being loaded
     * @return The updated record
     */
    private Recipe AddIngredientCount(Cursor cursor, Recipe currentModel) {
        cursor.moveToFirst();
        currentModel.setIngredientsNo(
                cursor.getInt(cursor.getColumnIndexOrThrow(
                        ProviderContract.RecipeIngredientEntry._COUNT)));
        return currentModel;
    }

    /**
     * Add the number of steps in the method to the current record being loaded
     * @return The updated record
     */
    private Recipe AddStepsCount(Cursor cursor, Recipe currentModel) {
        cursor.moveToFirst();
        currentModel.setStepsNo(cursor.getInt(
                cursor.getColumnIndexOrThrow(BaseColumns._COUNT))
        );
        return currentModel;
    }

    public interface ImagePermissionsListener {
        /**
         * Loader has requested a permission from calling activity
         */
        void onImagePermRequested();
    }
}
