package com.danthecodinggui.recipes.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.content.AsyncTaskLoader;

import com.danthecodinggui.recipes.model.FileUtils;
import com.danthecodinggui.recipes.model.ProviderContract;

import java.util.ArrayList;

public class GetRecipesLoader<D> extends AsyncTaskLoader {

    private ContentResolver contentResolver;

    private ArrayList<RecipeViewModel> records;

    public GetRecipesLoader(Context context) {
        super(context);
        contentResolver = context.getContentResolver();
        records = new ArrayList<>();
    }

    @Override
    public ArrayList<RecipeViewModel> loadInBackground() {

        //Call the content provider for the base data
        String[] baseProjection = {
                ProviderContract.RecipeEntry._ID,
                ProviderContract.RecipeEntry.VIEW_ORDER,
                ProviderContract.RecipeEntry.TITLE,
                ProviderContract.RecipeEntry.CALORIES_PER_PERSON,
                ProviderContract.RecipeEntry.DURATION,
                ProviderContract.RecipeEntry.IMAGE_PATH
        };

        String baseSortOrder = ProviderContract.RecipeEntry.VIEW_ORDER + " ASC";

        //Get all base values in list
        Cursor baseCursor = contentResolver.query(
                ProviderContract.RECIPES_URI,
                baseProjection,
                null,
                null,
                baseSortOrder
        );


        //Loop through all records
        RecipeViewModel temp;

        Cursor countCursor;

        int recordsGathered = 0;

        while (baseCursor.moveToNext()) {
            //Get base data
            temp = BuildBaseModel(baseCursor);

            String ingreSel = ProviderContract.RecipeIngredientEntry.RECIPE_ID + " = ?";
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

            //Add ingredient count
            temp = AddIngredientCount(countCursor, temp);

            String methodSel = ProviderContract.MethodStepEntry.RECIPE_ID + " = ?";

            countCursor = contentResolver.query(
                    ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.COUNT_PROJECTION,
                    methodSel,
                    countSelArgs,
                    null
            );

            //Add method steps count
            temp = AddStepsCount(countCursor, temp);

            //load in image from memory
            if (temp.hasPhoto()) {
                String path = Integer.toString(
                        baseCursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.IMAGE_PATH));
                temp.setPreview(FileUtils.GetImageFromFilePath(path));
            }

            records.add(temp);

            if (recordsGathered % 10 == 0)
                ;//call handler code to update ui with current list
            recordsGathered++;
        }
        baseCursor.close();

        return null;
    }

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
