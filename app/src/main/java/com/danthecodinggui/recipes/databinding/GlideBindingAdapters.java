package com.danthecodinggui.recipes.databinding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.danthecodinggui.recipes.R;

import java.io.File;

/**
 * Set of methods to be called when specific data binding attributes are set in layout files,
 * specifically those related to Glide image loading.
 */
public class GlideBindingAdapters {

    /**
     * Loads an image into an imageView from a local filepath
     * @param imageFilePath The path to the image
     * @param onLoadedListener A callback to be used for any actions once the image is loaded
     */
    @BindingAdapter(value = {"imageFilePath", "onLoadedListener"}, requireAll = false)
    public static void setImageResource(final ImageView view, String imageFilePath, RequestListener<Drawable> onLoadedListener) {

        //Get Uri from filepath
        Uri photoUri = Uri.fromFile(new File(imageFilePath));

        //Load an image (local storage) into an ImageView from a Uri

        final Context context = view.getContext();

        RequestOptions options = new RequestOptions()
                .dontTransform()
                .error(R.drawable.ic_imageload_error);

        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(photoUri)
                .listener(onLoadedListener)
                .into(view);

    }
}
