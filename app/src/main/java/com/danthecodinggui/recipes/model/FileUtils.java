package com.danthecodinggui.recipes.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;

public class FileUtils {

    public static Bitmap GetImageFromFilePath(String path) {

        //first need to ask for read external storage permission

        File publicDir = Environment.getExternalStorageDirectory();
        File imageFile = new File(publicDir + path);
        BitmapFactory.Options options = new BitmapFactory.Options();

        return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

    }
}
