package com.danthecodinggui.recipes.view.Loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;

/**
 * Load list of cachedRecords of a particular recipe
 */
public class GetIngredientsLoader extends AsyncTaskLoader<List<Ingredient>> {

    private ContentResolver contentResolver;

    private List<Ingredient> cachedRecords;

    private long recipePk;

    private ContentObserver ingredientsObserver;

    private Handler uiThread;

    public GetIngredientsLoader(@NonNull Context context, Handler uiThread, long recipePk) {
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

        if (ingredientsObserver == null) {
            ingredientsObserver = new ContentObserver(uiThread) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    onContentChanged();
                }
            };
            contentResolver.registerContentObserver(ProviderContract.RECIPES_URI, false, ingredientsObserver);
        }
    }

    @Nullable
    @Override
    public List<Ingredient> loadInBackground() {

        List<Ingredient> ingredients = new ArrayList<>();

        String[] projection = {
                ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME,
                ProviderContract.RecipeIngredientEntry.QUANTITY,
                ProviderContract.RecipeIngredientEntry.MEASUREMENT
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

        Ingredient temp;

        while (cursor.moveToNext()) {

            temp = new Ingredient(
                    cursor.getString(cursor.getColumnIndexOrThrow(
                            ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME)),
                    cursor.getInt(cursor.getColumnIndex(
                            ProviderContract.RecipeIngredientEntry.QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(
                            ProviderContract.RecipeIngredientEntry.MEASUREMENT))
                    );
            ingredients.add(temp);
        }
        cursor.close();

        return ingredients;
    }

    @Override
    public void deliverResult(List<Ingredient> data) {
        if (isReset()) {
            cachedRecords = null;
            return;
        }

        List<Ingredient> oldCache = cachedRecords;
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

        if (ingredientsObserver != null) {
            contentResolver.unregisterContentObserver(ingredientsObserver);
            ingredientsObserver = null;
        }
    }
}
