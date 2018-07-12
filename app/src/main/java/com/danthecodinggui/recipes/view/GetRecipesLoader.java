package com.danthecodinggui.recipes.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.msc.LogTags;

import java.util.ArrayList;
import java.util.List;

public class GetRecipesLoader extends UpdatingAsyncTaskLoader {

    private Handler uiThread;

    private ContentResolver contentResolver;

    private List<RecipeViewModel> records;

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

    GetRecipesLoader(Context context, Handler uiThread, ProgressUpdateListener updateCallback, int loaderId) {
        super(context, uiThread, updateCallback, loaderId);
        contentResolver = context.getContentResolver();
        records = new ArrayList<>();
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (records != null)
            deliverResult(records);
        else
            forceLoad();
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
            String[] countSelArgs = {Long.toString(baseCursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry._ID
            ))};

            countCursor = contentResolver.query(
                    ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.COUNT_PROJECTION,
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
                    ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.COUNT_PROJECTION,
                    methodSel,
                    countSelArgs,
                    null
            );
            if (countCursor == null)
                Log.e(LogTags.CONTENT_PROVIDER, "Previews Loader: method cursor is null");
            else
                temp = AddStepsCount(countCursor, temp);

            /*
            //Load in preview image
            if (temp.hasPhoto()) {
                String path = Integer.toString(
                        baseCursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.IMAGE_PATH));

                temp.setPreview(FileUtils.GetImageFromFilePath(path));
            }*/

            records.add(temp);

            recordsGathered = records.size();

            //In case there's a vast amount of records, update the ui every 10 loaded records
            if (recordsGathered % 10 == 0)
                UpdateProgress(records.subList(recordsGathered - 10, recordsGathered - 1));
        }
        baseCursor.close();

        //Return remaining records in list
        return records.subList(recordsGathered - (recordsGathered % 10) - 1, recordsGathered - 1);
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
                    return new RecipeViewModel(recipeTitle, false);
                else
                    return new RecipeViewModel(recipeTitle, timeInMins, false);
            } else {
                if (noTime)
                    return new RecipeViewModel(recipeTitle, calories, false);
                else
                    return new RecipeViewModel(recipeTitle, calories, timeInMins, false);
            }
        } else {
            if (noKcal) {
                if (noTime)
                    return new RecipeViewModel(recipeTitle, true);
                else
                    return new RecipeViewModel(recipeTitle, timeInMins, true);
            } else {
                if (noTime)
                    return new RecipeViewModel(recipeTitle, calories, true);
                else
                    return new RecipeViewModel(recipeTitle, calories, timeInMins, true);
            }
        }
    }

    private RecipeViewModel AddIngredientCount(Cursor cursor, RecipeViewModel currentModel) {
        cursor.moveToFirst();
        currentModel.setIngredientsNo(
                cursor.getColumnIndexOrThrow(ProviderContract.RecipeIngredientEntry._COUNT));
        return currentModel;
    }

    private RecipeViewModel AddStepsCount(Cursor cursor, RecipeViewModel currentModel) {
        cursor.moveToFirst();
        currentModel.setStepsNo(
                cursor.getColumnIndexOrThrow(ProviderContract.RecipeIngredientEntry._COUNT)
        );
        return currentModel;
    }
}
