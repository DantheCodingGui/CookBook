package com.danthecodinggui.recipes.view.view_recipe;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.view.UpdatingAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

public class GetIngredientsLoader extends UpdatingAsyncTaskLoader {

    ContentResolver contentResolver;

    private List<String> ingredients;

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

        String[] arguments = { Long.toString(recipePk) };

        Cursor cursor = contentResolver.query(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                projection,
                ProviderContract.INGREDIENTS_SELECTION,
                arguments,
                null
        );

        int recordsGathered = 0;

        while (cursor.moveToNext()) {
            ingredients.add(cursor.getString(
                    cursor.getColumnIndexOrThrow(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME)));


            recordsGathered = ingredients.size();
            if (recordsGathered % 10 == 0)
                UpdateProgress(ingredients.subList(recordsGathered - 10, recordsGathered - 1));
        }
        cursor.close();

        return ingredients.subList(recordsGathered - (recordsGathered % 10), recordsGathered);
    }
}
