package com.danthecodinggui.recipes.view.activity_home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.danthecodinggui.recipes.model.ProviderContract;

import java.util.List;

/**
 * Delete single/multiple recipes and it's steps/ingredients
 */
public class DeleteRecipeTask extends AsyncTask<List<Long>, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;

    DeleteRecipeTask(Context context) { this.context = context; }

    @Override
    protected Void doInBackground(List<Long>... params) {

        List<Long> recipesToDelete = params[0];

        if (recipesToDelete == null)
            return null;

        for (Long primaryKey: recipesToDelete) {

            //First delete base recipe record
            context.getContentResolver().delete(
                    ProviderContract.RECIPES_URI,
                    ProviderContract.RecipeEntry._ID + " = ?",
                    new String[]{Long.toString(primaryKey)}
            );

            //Then delete all the steps
            context.getContentResolver().delete(
                    ProviderContract.METHOD_URI,
                    ProviderContract.MethodStepEntry.RECIPE_ID + " = ?",
                    new String[]{Long.toString(primaryKey)}
            );

            //Finally all the ingredients
            context.getContentResolver().delete(
                    ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.RecipeIngredientEntry.RECIPE_ID + " = ?",
                    new String[]{Long.toString(primaryKey)}
            );

        }

        context = null;

        return null;
    }
}
