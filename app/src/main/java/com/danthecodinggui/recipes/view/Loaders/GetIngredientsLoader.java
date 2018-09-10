package com.danthecodinggui.recipes.view.Loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

/**
 * Load list of ingredients of a particular recipe
 */
public class GetIngredientsLoader extends UpdatingAsyncTaskLoader {

    private ContentResolver contentResolver;

    private List<Ingredient> ingredients;

    private long recipePk;

    public GetIngredientsLoader(@NonNull Context context, Handler uiThread,
                                ProgressUpdateListener progressCallback, int loaderId, long recipePk) {
        super(context, uiThread, progressCallback, loaderId);

        contentResolver = context.getContentResolver();
        this.recipePk = recipePk;
    }

    @Override
    protected void onStartLoading() {
        //Cache records so can handle orientation changes and such
        if (ingredients != null) {
            Log.v(DATA_LOADING, "Ingredients loading started: Using cached values");
            deliverResult(ingredients);
        }
        else {
            Log.v(DATA_LOADING, "Ingredients loading started: Load new values");
            ingredients = new ArrayList<>();
            forceLoad();
        }
    }

    @Nullable
    @Override
    public Object loadInBackground() {

        String[] projection = {
                ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME
        };

        //Link to recipe _id
        String[] arguments = { Long.toString(recipePk) };

        Cursor cursor = contentResolver.query(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                projection,
                ProviderContract.INGREDIENTS_SELECTION,
                arguments,
                null
        );

        int recordsGathered = 0;

        Ingredient temp;

        while (cursor.moveToNext()) {

            temp = new Ingredient(cursor.getString(
                    cursor.getColumnIndexOrThrow(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME)));
            ingredients.add(temp);

            recordsGathered = ingredients.size();
            if (recordsGathered % 10 == 0)
                UpdateProgress(ingredients.subList(recordsGathered - 10, recordsGathered - 1));
        }
        cursor.close();

        return ingredients.subList(recordsGathered - (recordsGathered % 10), recordsGathered);
    }
}