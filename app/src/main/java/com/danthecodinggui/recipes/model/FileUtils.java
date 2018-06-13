package com.danthecodinggui.recipes.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;

public class FileUtils {

    public static Bitmap GetImageFromFilePath(String path) {
        // TODO async call then return (probs asynctaskloader)
        // need to provide callback to lead back to mainactivity, will add bitmap
        //first need to ask for read external storage permission

        File publ = Environment.getExternalStorageDirectory();
        File imageFile = new File(publ + path);
        BitmapFactory.Options op = new BitmapFactory.Options();

        Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), op);

        return image;
    }

    public static String SaveImage(Bitmap image) {
        // TODO implement async save to file and return filepath for save to content provider
        return null;
    }
}
