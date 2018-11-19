package com.danthecodinggui.recipes.databinding;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.utility.Utility;

import java.io.File;

/**
 * Set of methods to be called when specific data binding attributes are set in layout files,
 * specifically those related to Glide image loading.
 */
public class BindingAdapters {

    /**
     * Loads an image into an imageView from a local filepath </br>
     * NOTE: Cannot use file path and url in the same view, will prioritise file path
     * @param imagePath The path to the image (either local or remote)
     * @param onLoadedListener A callback to be used for any actions once the image is loaded
     */
    @BindingAdapter(value = {"imagePath", "onLoadedListener"}, requireAll = false)
    public static void setImageResource(final ImageView view, String imagePath, RequestListener<Drawable> onLoadedListener) {

        if (imagePath == null)
            return;

        final Context context = view.getContext();

        //Allows both filepath and url based image locations, let glide deal with actually getting them

        boolean isURL = Patterns.WEB_URL.matcher(imagePath).matches();
        boolean isFilepath = new File(imagePath).exists();

        //checks for if url, if filepath, and if image url

        RequestOptions options = new RequestOptions()
                .dontTransform()
                .error(R.drawable.ic_imageload_error);

        if (isURL) {
            //Just load from URL
            Glide.with(context)
                    .setDefaultRequestOptions(options)
                    .load(imagePath)
                    .listener(onLoadedListener)
                    .into(view);
        }
        else if (isFilepath) {
            //String is a file
            //Get Uri from filepath
            Uri imageUri = Uri.fromFile(new File(imagePath));

            Glide.with(context)
                    .setDefaultRequestOptions(options)
                    .load(imageUri)
                    .listener(onLoadedListener)
                    .into(view);
        }

    }

    @BindingAdapter(value = {"stepNum", "stepText"})
    public static void setMethodText(final TextView view, int stepNum, String stepText) {

        //Add full stop if not already there
        if (!stepText.substring(stepText.length() - 1).equals("."))
            stepText = stepText.concat(".");

        Resources res = view.getContext().getResources();
        view.setText(res.getString(R.string.txt_method_step_item, stepNum, stepText));
    }

    @BindingAdapter(value = "shouldShowLandscapePadding")
    public static void setLandscapeLayout(View view, boolean isLandscapeLayout) {

        Context context = view.getContext();
        int landscapePadding = Utility.dpToPx(context, 99);
        int portraitPadding = Utility.dpToPx(context, 19);

        if (isLandscapeLayout)
            view.setPadding(landscapePadding, 0, landscapePadding, 0);
        else
            view.setPadding(portraitPadding, 0, portraitPadding, 0);
    }
}
