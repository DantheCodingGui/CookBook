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
import android.util.Log;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.Utility;

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
import static com.danthecodinggui.recipes.msc.LogTags.SAVE_RECIPE;

/**
 * Inserts a recipe in full into permanent storage
 */
public class SaveRecipeTask extends AsyncTask<Bundle, Void, Void> {

    private static final String RECIPES_CAM_DIR_NAME = "/Recipes/";

    @SuppressLint("StaticFieldLeak")
    private Context context;

    public SaveRecipeTask(Context context) {
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
            //Check that external storage is available for write
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

                //First must read image file from private storage
                Bitmap camImage = BitmapFactory.decodeFile(item.getImagePath());

                //Then save the image into external storage (recipes folder)
                File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), RECIPES_CAM_DIR_NAME);
                String dirPath = dirFile.getPath();
                Utility.CreateDir(dirPath);

                File imageFile = new File(new File(dirPath), CreateFileName(dirPath, item.getTitle(),0));

                try {
                    FileOutputStream out = new FileOutputStream(imageFile);
                    camImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    Log.e(SAVE_RECIPE, "External recipes directory not found", e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(SAVE_RECIPE, "Saving camera bitmap failed", e);
                    e.printStackTrace();
                }

                //Alert media scanner that new file has been added
                MediaScannerConnection.scanFile(
                        context,
                        new String[] {imageFile.getAbsolutePath()},
                        null,
                        null);

                item.setImagePath(imageFile.getPath());
            }
        }

        //Now can delete all camera files in the private storage directory
        Utility.ClearDir(camDirPath);

        //Save the recipe into database record

        ContentResolver resolver = context.getContentResolver();

        ContentValues record = new ContentValues();

        //TODO remove vieworder later when sorting implemented
        record.put(ProviderContract.RecipeEntry.VIEW_ORDER, 1);
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
