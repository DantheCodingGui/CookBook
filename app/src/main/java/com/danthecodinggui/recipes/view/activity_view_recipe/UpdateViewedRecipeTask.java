package com.danthecodinggui.recipes.view.activity_view_recipe;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.utility.Utility;

public class UpdateViewedRecipeTask extends AsyncTask<Long, Void, Recipe> {

    private onRecipeReadListener callback;

    private ContentResolver contentResolver;

    UpdateViewedRecipeTask(Context context,  onRecipeReadListener callback) {
        this.callback = callback;
        contentResolver = context.getContentResolver();
    }

    @Override
    protected Recipe doInBackground(Long[] params) {

        long recipeId = params[0];

        Cursor cursor = contentResolver.query(
                ProviderContract.RECIPES_URI,
                ProviderContract.RECIPE_PROJECTION_FULL,
                ProviderContract.RecipeEntry._ID + " = ?",
                new String[] {Long.toString(recipeId)},
                null
        );

        cursor.moveToFirst();
        return Utility.BuildModelFromCursor(cursor);
    }

    @Override
    protected void onPostExecute(Recipe updatedRecipe) {
        callback.onRecipeRead(updatedRecipe);
    }

    interface onRecipeReadListener {
        void onRecipeRead(Recipe updatedRecipe);
    }
}
