package com.danthecodinggui.recipes.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.RecipeViewModel;
import com.danthecodinggui.recipes.msc.LogTags;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

public class GetRecipesLoader extends UpdatingAsyncTaskLoader {

    private ContentResolver contentResolver;

    private List<RecipeViewModel> records;

    private ImagePermissionsListener permissionsCallback;

    //Content provider query data
    private final String[] recipesProjection = {
            ProviderContract.RecipeEntry._ID,
            ProviderContract.RecipeEntry.VIEW_ORDER,
            ProviderContract.RecipeEntry.TITLE,
            ProviderContract.RecipeEntry.CALORIES_PER_PERSON,
            ProviderContract.RecipeEntry.DURATION,
            ProviderContract.RecipeEntry.IMAGE_PATH
    };
    private final String recipesSortOrder = ProviderContract.RecipeEntry.VIEW_ORDER + " ASC";
    private final String ingreSel = ProviderContract.RecipeIngredientEntry.RECIPE_ID + " = ?";
    private final String methodSel = ProviderContract.MethodStepEntry.RECIPE_ID + " = ?";

    GetRecipesLoader(Context context, Handler uiThread, ProgressUpdateListener updateCallback,
                     ImagePermissionsListener permissionCallback, int loaderId) {
        super(context, uiThread, updateCallback, loaderId);
        contentResolver = context.getContentResolver();
        this.permissionsCallback = permissionCallback;
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (records != null) {
            Log.v(DATA_LOADING, "Recipe loading started: Using cached values");
            deliverResult(records);
        }
        else {
            Log.v(DATA_LOADING, "Recipe loading started: Load new values");
            records = new ArrayList<>();
            forceLoad();
        }
    }

    @Override
    public List<RecipeViewModel> loadInBackground() {

        //Query recipes table for all records
        Cursor baseCursor = contentResolver.query(
                ProviderContract.RECIPES_URI,
                recipesProjection,
                null,
                null,
                recipesSortOrder
        );
        //Cursor for the ingredients/method queries afterwards
        Cursor countCursor;

        //Temporary holder variable
        RecipeViewModel temp;

        int recordsGathered = 0;

        while (baseCursor.moveToNext()) {
            //Get base recipe data
            temp = BuildBaseModel(baseCursor);

                    //Add ingredients data to record
            String[] countSelArgs = { Long.toString(
                    baseCursor.getLong(baseCursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry._ID))) };

            //syntax error (code 1): , while compiling: SELECT count(_id) AS count FROM SELECT _id, RecipeId, IngredientName FROM RecipeIngredients INNER JOIN Ingredients ON IngredientId = _id WHERE (RecipeId = ?)

            countCursor = contentResolver.query(
                    ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.COUNT_INGREDIENTS_PROJECTION,
                    ingreSel,
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
                    methodSel,
                    countSelArgs,
                    null
            );
            if (countCursor == null)
                Log.e(LogTags.CONTENT_PROVIDER, "Previews Loader: method cursor is null");
            else
                temp = AddStepsCount(countCursor, temp);


            //Need to ask for permission if a recipe includes a photo
            if (temp.hasPhoto() && permissionsCallback != null)
                AskForReadPermission();

            records.add(temp);

            recordsGathered = records.size();

            //In case there's a vast amount of records, update the ui every 10 loaded records
            if (recordsGathered % 10 == 0)
                UpdateProgress(records.subList(recordsGathered - 10, recordsGathered - 1));
        }
        baseCursor.close();

        //Return remaining records in list
        return records.subList(recordsGathered - (recordsGathered % 10), recordsGathered);
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
    }

    /**
     * Decides what type of RecipeViewModel to build based on what data is saved in the table
     * record
     * @return The base RecipeViewModel constructed with the data found in the record
     */
    private RecipeViewModel BuildBaseModel(Cursor cursor) {

        String recipeTitle = cursor.getString(cursor.getColumnIndexOrThrow(
                ProviderContract.RecipeEntry.TITLE));
        int calories = -1;
        int timeInMins = -1;

        boolean noImage = cursor.isNull(cursor.getColumnIndexOrThrow(
                ProviderContract.RecipeEntry.IMAGE_PATH));
        boolean noKcal = cursor.isNull(cursor.getColumnIndexOrThrow(
                ProviderContract.RecipeEntry.CALORIES_PER_PERSON));
        boolean noTime = cursor.isNull(cursor.getColumnIndexOrThrow(
                ProviderContract.RecipeEntry.DURATION));

        if (!noKcal)
            calories = cursor.getInt(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.CALORIES_PER_PERSON));
        if (!noTime)
            timeInMins = cursor.getInt(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.DURATION));

        if (noImage) {
            if (noKcal) {
                if (noTime)
                    return new RecipeViewModel(recipeTitle);
                else
                    return new RecipeViewModel(recipeTitle, timeInMins);
            } else {
                if (noTime)
                    return new RecipeViewModel(recipeTitle, calories);
                else
                    return new RecipeViewModel(recipeTitle, calories, timeInMins);
            }
        } else {

            String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.IMAGE_PATH));

            if (noKcal) {
                if (noTime)
                    return new RecipeViewModel(recipeTitle, photoPath);
                else
                    return new RecipeViewModel(recipeTitle, timeInMins, photoPath);
            } else {
                if (noTime)
                    return new RecipeViewModel(recipeTitle, calories, photoPath);
                else
                    return new RecipeViewModel(recipeTitle, calories, timeInMins, photoPath);
            }
        }
    }

    /**
     * Add the number of ingredients to the current record being loaded
     * @return The updated record
     */
    private RecipeViewModel AddIngredientCount(Cursor cursor, RecipeViewModel currentModel) {
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
    private RecipeViewModel AddStepsCount(Cursor cursor, RecipeViewModel currentModel) {
        cursor.moveToFirst();
        currentModel.setStepsNo(cursor.getInt(
                cursor.getColumnIndexOrThrow(BaseColumns._COUNT))
        );
        return currentModel;
    }

    interface ImagePermissionsListener {
        /**
         * Loader has requested a permission from calling activity
         */
        void onImagePermRequested();
    }
}
