package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.utility.FileUtils;

import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_CAMERA_DIR_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_IS_IMAGE_CAMERA;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_METHOD;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_OLD_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_OLD_METHOD;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_RECIPE;

/**
 * Inserts a recipe in full into permanent storage
 */
public class UpdateRecipeTask extends AsyncTask<Bundle, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;

    public UpdateRecipeTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Bundle... params) {

        Bundle param = params[0];

        //Base info
        Recipe item = param.getParcelable(SAVE_TASK_RECIPE);
        List<Ingredient> newIngredients = param.getParcelableArrayList(SAVE_TASK_INGREDIENTS);
        List<MethodStep> newMethod = param.getParcelableArrayList(SAVE_TASK_METHOD);

        //Info related to camera
        boolean isImageFromCam = param.getBoolean(SAVE_TASK_IS_IMAGE_CAMERA);
        String camDirPath = param.getString(SAVE_TASK_CAMERA_DIR_PATH);

        //Deal with saving photo to external storage if from camera
        //Can use same implementation as SaveRecipeTask, as isImageFromCam will be false if
        // image already saved
        if (item.hasPhoto() && isImageFromCam) {

            String externPhotoPath = FileUtils.SavePhotoToExternalDir(context, item);
            if (externPhotoPath != null)
                item.setImagePath(externPhotoPath);

        }

        //Now can delete all camera files in the private storage directory
        FileUtils.ClearDir(camDirPath);

        //First update recipe entry
        long recipeId = item.getRecipeId();
        String[] selectionArgs =  new String[] {Long.toString(recipeId)};


        ContentResolver resolver = context.getContentResolver();

        ContentValues record;

        //Update ingredients/method first to ensure when recipe content listeners fired, everything is updated

        List<Ingredient> oldIngredients = param.getParcelableArrayList(SAVE_TASK_OLD_INGREDIENTS);
        List<MethodStep> oldMethod = param.getParcelableArrayList(SAVE_TASK_OLD_METHOD);

        //First we only proceed with update if there is a change
        if (!oldIngredients.equals(newIngredients)) {

            //Easiest solution is to delete all currently stored ingredients and re-add the new ones
            resolver.delete(ProviderContract.RECIPE_INGREDIENTS_URI,
                    ProviderContract.RecipeIngredientEntry.RECIPE_ID + " = ?",
                   selectionArgs);

            //Save each new ingredient (loop)
            for (Ingredient i : newIngredients) {
                record = new ContentValues();
                record.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
                record.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, i.getIngredientText());
                record.put(ProviderContract.RecipeIngredientEntry.QUANTITY, i.getQuantity());
                record.put(ProviderContract.RecipeIngredientEntry.MEASUREMENT, i.getMeasurement());
                resolver.insert(ProviderContract.RECIPE_INGREDIENTS_URI, record);
            }
        }

        if (!oldMethod.equals(newMethod)) {

            resolver.delete(ProviderContract.METHOD_URI,
                    ProviderContract.MethodStepEntry .RECIPE_ID + " = ?",
                    selectionArgs);

            //Save each new method step (loop)
            for (MethodStep s: newMethod) {
                record = new ContentValues();
                record.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
                record.put(ProviderContract.MethodStepEntry.STEP_NO, s.getStepNumber());
                record.put(ProviderContract.MethodStepEntry.STEP_TEXT, s.getStepText());
                resolver.insert(ProviderContract.METHOD_URI, record);
            }
        }

        record = new ContentValues();

        record.put(ProviderContract.RecipeEntry.TITLE, item.getTitle());

        //optional data
        int duration = item.getTimeInMins();
        int kcal = item.getCalories();
        if (duration != 0)
            record.put(ProviderContract.RecipeEntry.DURATION, duration);
        else
            record.putNull(ProviderContract.RecipeEntry.DURATION);
        if (kcal != 0)
            record.put(ProviderContract.RecipeEntry.CALORIES_PER_PERSON, kcal);
        else
            record.putNull(ProviderContract.RecipeEntry.CALORIES_PER_PERSON);
        if (item.hasPhoto())
            record.put(ProviderContract.RecipeEntry.IMAGE_PATH, item.getImagePath());
        else
            record.putNull(ProviderContract.RecipeEntry.IMAGE_PATH);

        resolver.update(ProviderContract.RECIPES_URI,
                record,
                ProviderContract.RecipeEntry._ID + " = ?",
                selectionArgs);

        context = null;

        return null;
    }
}
