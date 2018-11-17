package com.danthecodinggui.recipes.view.Loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.LogTags;
import com.danthecodinggui.recipes.msc.utility.FileUtils;
import com.danthecodinggui.recipes.msc.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.PREF_KEY_HOME_SORT_DIR;
import static com.danthecodinggui.recipes.msc.GlobalConstants.PREF_KEY_HOME_SORT_ORDER;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SORT_ORDER_ALPHABETICAL;
import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

/**
 * Loads recipes from database into HomeActivity view
 */
public class GetRecipesLoader extends AsyncTaskLoader<List<Recipe>>
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ContentResolver contentResolver;

    private List<Recipe> cachedRecords;

    private ImagePermissionsListener permissionsCallback;

    private boolean waitingForPermissionResponse = false;

    private ContentObserver contentObserver;

    private Handler uiThread;

    private int recipesSortOrder;
    private boolean isSortDirAsc;

    public GetRecipesLoader(Context context, Handler uiThread,
                     ImagePermissionsListener permissionCallback,
                        int recipesSortOrder, boolean recipesSortDir) {
        super(context);
        this.uiThread = uiThread;
        contentResolver = context.getContentResolver();
        this.permissionsCallback = permissionCallback;
        this.recipesSortOrder = recipesSortOrder;
        this.isSortDirAsc = recipesSortDir;
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (cachedRecords != null) {
            Log.v(DATA_LOADING, "Recipe loading started: Using cached values");
            deliverResult(cachedRecords);
        }
        if (takeContentChanged() || cachedRecords == null){
            Log.v(DATA_LOADING, "Recipe loading started: Load new values");
            forceLoad();
        }

        if (contentObserver == null) {
            contentObserver = new ContentObserver(uiThread) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    onContentChanged();
                }
            };
            contentResolver.registerContentObserver(ProviderContract.METHOD_URI, false, contentObserver);
        }
    }


    @Override
    public List<Recipe> loadInBackground() {

        List<Recipe> records = new ArrayList<>();

        //Query recipes table for all records
        Cursor baseCursor = contentResolver.query(
                ProviderContract.RECIPES_URI,
                ProviderContract.RECIPE_PROJECTION_FULL,
                null,
                null,
                 ParseSortOrder()
        );
        //Cursor for the ingredients/method queries afterwards
        Cursor countCursor;

        //Temporary holder variable
        Recipe temp;

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


            //Need to ask for permission if a recipe includes a local photo
            if (temp.hasPhoto() && FileUtils.isImageLocal(temp.getImagePath()) && permissionsCallback != null) {
                AskForReadPermission();

                while (waitingForPermissionResponse) {
                    //Busy waiting, but check 10 times a second
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            records.add(temp);
        }
        baseCursor.close();

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

    private String ParseSortOrder() {
        String sortDir = isSortDirAsc ? " ASC" : " DESC";

        if (recipesSortOrder == SORT_ORDER_ALPHABETICAL)
            return ProviderContract.RecipeEntry.TITLE + sortDir;
        else
            return ProviderContract.RecipeEntry._ID + sortDir;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefKey) {
        switch (prefKey) {
            case PREF_KEY_HOME_SORT_ORDER:
                recipesSortOrder = sharedPreferences.getInt(prefKey, SORT_ORDER_ALPHABETICAL);

                onRecipeSortChanged();
                break;
            case PREF_KEY_HOME_SORT_DIR:
                isSortDirAsc = sharedPreferences.getBoolean(prefKey, true);

                onRecipeSortChanged();
                break;
        }
    }

    private void onRecipeSortChanged() {

        if (cachedRecords == null) {
            startLoading();
            return;
        }

        Comparator<Recipe> comparator;

        //Sort cached list and resend to activity
        if (recipesSortOrder == SORT_ORDER_ALPHABETICAL)
            comparator = new Comparator<Recipe>() {
                @Override
                public int compare(Recipe recipe1, Recipe recipe2) {
                    if (isSortDirAsc)
                        return recipe1.getTitle().compareToIgnoreCase(recipe2.getTitle());
                    else
                        return recipe2.getTitle().compareToIgnoreCase(recipe1.getTitle());
                }
            };
        else
            comparator = new Comparator<Recipe>() {
                @Override
                public int compare(Recipe recipe1, Recipe recipe2) {
                    if (isSortDirAsc ?
                            (recipe1.getRecipeId() > recipe2.getRecipeId()) :
                            (recipe1.getRecipeId() < recipe2.getRecipeId()))
                        return 1;
                    else
                        return -1;
                }
            };

        Collections.sort(cachedRecords, comparator);
        onStartLoading();
    }

    public interface ImagePermissionsListener {
        /**
         * Loader has requested a permission from calling activity
         */
        void onImagePermRequested();
    }
}
