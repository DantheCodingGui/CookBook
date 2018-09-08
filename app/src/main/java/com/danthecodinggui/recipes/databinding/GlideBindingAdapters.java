package com.danthecodinggui.recipes.databinding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CircularProgressDrawable;
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

    @BindingAdapter(value = {"imageFilePath", "onLoadedListener"}, requireAll = false)
    public static void setImageResource(final ImageView view, String imageFilePath, RequestListener<Drawable> onLoadedListener) {

        //Get Uri from filepath
        Uri photoUri = Uri.fromFile(new File(imageFilePath));

        //Load an image (local storage) into an ImageView from a Uri

        final Context context = view.getContext();

        //Make placeholder spinner
        CircularProgressDrawable placeholder = new CircularProgressDrawable(context);
        placeholder.setStrokeWidth(5f);
        placeholder.setCenterRadius(50f);
        placeholder.start();

        //Todo update placeholder and error with final custom ones
        RequestOptions options = new RequestOptions()
                .placeholder(placeholder)
                .error(R.drawable.ic_imageload_error);

        Glide.with(context)
                .setDefaultRequestOptions(options)
                .load(photoUri)
                .listener(onLoadedListener)
                .into(view);
    }
}
