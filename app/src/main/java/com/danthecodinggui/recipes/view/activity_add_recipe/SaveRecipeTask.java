package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.media.ExifInterface;
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.utility.FileUtils;
import com.danthecodinggui.recipes.msc.utility.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_CAMERA_DIR_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_IS_IMAGE_CAMERA;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_METHOD;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_RECIPE;

/**
 * Inserts a recipe in full into permanent storage
 */
public class SaveRecipeTask extends AsyncTask<Bundle, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;

    SaveRecipeTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Bundle... params) {

        Bundle param = params[0];

        //Base info
        Recipe item = param.getParcelable(SAVE_TASK_RECIPE);
        List<Ingredient> ingredients = param.getParcelableArrayList(SAVE_TASK_INGREDIENTS);
        List<MethodStep> method = param.getParcelableArrayList(SAVE_TASK_METHOD);

        //Info related to camera
        boolean isImageFromCam = param.getBoolean(SAVE_TASK_IS_IMAGE_CAMERA);
        String camDirPath = param.getString(SAVE_TASK_CAMERA_DIR_PATH);

        //Deal with saving photo to external storage if from camera
        if (item.hasPhoto() && isImageFromCam) {

            String externPhotoPath = FileUtils.SavePhotoToExternalDir(context, item);
            if (externPhotoPath != null)
                item.setImagePath(externPhotoPath);
        }

        //Now can delete all camera files in the private storage directory
        FileUtils.ClearDir(camDirPath);

        //Save the recipe into database record

        ContentResolver resolver = context.getContentResolver();

        ContentValues record = new ContentValues();

        record.put(ProviderContract.RecipeEntry.TITLE, item.getTitle());

        //optional data
        int duration = item.getTimeInMins();
        int kcal = item.getCalories();
        if (duration != 0)
            record.put(ProviderContract.RecipeEntry.DURATION, duration);
        if (kcal != 0)
            record.put(ProviderContract.RecipeEntry.CALORIES_PER_PERSON, kcal);
        if (item.hasPhoto())
            record.put(ProviderContract.RecipeEntry.IMAGE_PATH, item.getImagePath());

        Uri result = resolver.insert(ProviderContract.RECIPES_URI, record);
        long recipeId = ContentUris.parseId(result);

        //Save each ingredient (loop)
        for (Ingredient i: ingredients) {
            record = new ContentValues();
            record.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
            record.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, i.getIngredientText());
            record.put(ProviderContract.RecipeIngredientEntry.QUANTITY, i.getQuantity());
            record.put(ProviderContract.RecipeIngredientEntry.MEASUREMENT, i.getMeasurement());
            resolver.insert(ProviderContract.RECIPE_INGREDIENTS_URI, record);
        }

        //Save each method step (loop)
        for (MethodStep s: method) {
            record = new ContentValues();
            record.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
            record.put(ProviderContract.MethodStepEntry.STEP_NO, s.getStepNumber());
            record.put(ProviderContract.MethodStepEntry.STEP_TEXT, s.getStepText());
            resolver.insert(ProviderContract.METHOD_URI, record);
        }


        resolver.notifyChange(ProviderContract.RECIPES_URI, null);

        context = null;

        return null;
    }

    /**
     * Creates filename guaranteed to not already exist (supports duplicate filenames)
     * @param parentPath The Recipes Directory path
     * @param recipeTitle The title of the recipe (used as a basis for the filename)
     * @param duplicateNum Used for recursive implementation (call with 0)
     * @return Unique filename based on recipe title
     */
    private String CreateFileName(String parentPath, String recipeTitle, int duplicateNum) {
        String suffix = "";
        if (duplicateNum != 0)
            suffix = "(" + duplicateNum + ")";

        String fileName = recipeTitle.replaceAll(" ", "_") + suffix + ".jpg";

        if (new File(parentPath, fileName).exists())
            CreateFileName(parentPath, recipeTitle, ++duplicateNum);
        return fileName;
    }
}
