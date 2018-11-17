package com.danthecodinggui.recipes.msc.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.media.ExifInterface;
import android.util.Log;
import android.util.Patterns;

import com.danthecodinggui.recipes.model.object_models.Recipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.danthecodinggui.recipes.msc.LogTags.SAVE_RECIPE;

/**
 * Set of utility methods related to data storage
 */
public class FileUtils {

    //Path from external public directory to application-specific directory for storing images from
    //the camera
    private static final String RECIPES_CAM_DIR_NAME = "/Recipes/";

    public static boolean DeleteFile(String path) {

        if (path == null)
            return false;

        File imageFile = new File(path);
        if (imageFile.exists())
            return imageFile.delete();
        return false;
    }

    public static void ClearDir(String dirPath) {

        if (dirPath == null)
            return;

        File dir = new File(dirPath);

        if (dir.isDirectory())
            for (File child: dir.listFiles())
                child.delete();
    }

    public static boolean CreateDir(String path) {

        if (path == null)
            return false;

        File dir = new File(path);
        if (!dir.exists())
            return dir.mkdir();
        return false;
    }

    public static boolean ImageExists(String imagePath) {
        if (Patterns.WEB_URL.matcher(imagePath).matches())
            return true;
        return isImageLocal(imagePath);
    }

    public static boolean isImageLocal(String filepath) {
        return new File(filepath).exists();
    }

    public static String SavePhotoToExternalDir(Context context, Recipe item) {
        //Check that external storage is available for write
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            //First must read image file from private storage
            Bitmap camImage = BitmapFactory.decodeFile(item.getImagePath());

            //Also must extract bitmap internal rotation
            int internalOrientation = ExifInterface.ORIENTATION_NORMAL;
            try {
                internalOrientation = new ExifInterface(item.getImagePath()).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException e) {
                Log.e(SAVE_RECIPE, "Failed to read Exif information from internal image file", e);
                e.printStackTrace();
            }

            //Then save the image into external storage (recipes folder)
            File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), RECIPES_CAM_DIR_NAME);
            String dirPath = dirFile.getPath();
            CreateDir(dirPath);

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

            //As Exif orientation will be overwritten, need to preserve orientation in new file
            if (internalOrientation != ExifInterface.ORIENTATION_NORMAL) {
                try {
                    ExifInterface externalMetaData = new ExifInterface(imageFile.getAbsolutePath());
                    externalMetaData.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(internalOrientation));
                    externalMetaData.saveAttributes();
                } catch (IOException e) {
                    Log.e(SAVE_RECIPE, "Failed to copy file Exif information to external file", e);
                    e.printStackTrace();
                }
            }

            //Alert media scanner that new file has been added
            MediaScannerConnection.scanFile(
                    context,
                    new String[] {imageFile.getAbsolutePath()},
                    null,
                    null);

            return imageFile.getPath();
        }

        return null;
    }

    /**
     * Creates filename guaranteed to not already exist (supports duplicate filenames)
     * @param parentPath The Recipes Directory path
     * @param recipeTitle The title of the recipe (used as a basis for the filename)
     * @param duplicateNum Used for recursive implementation (call with 0)
     * @return Unique filename based on recipe title
     */
    private static String CreateFileName(String parentPath, String recipeTitle, int duplicateNum) {
        String suffix = "";
        if (duplicateNum != 0)
            suffix = "(" + duplicateNum + ")";

        String fileName = recipeTitle.replaceAll(" ", "_") + suffix + ".jpg";

        if (new File(parentPath, fileName).exists())
            CreateFileName(parentPath, recipeTitle, ++duplicateNum);
        return fileName;
    }
}
