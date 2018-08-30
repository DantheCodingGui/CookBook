package com.danthecodinggui.recipes.databinding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.R;

import java.io.File;

import static com.danthecodinggui.recipes.msc.LogTags.GLIDE;

/**
 * Set of methods to be called when specific data binding attributes are set in layout files,
 * specifically those related to Glide image loading.
 */
public class GlideBindingAdapters {

    @BindingAdapter("imageFilePath")
    public static void setImageResource(final ImageView view, String imageFilePath) {

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
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        ((FragmentActivity)context).supportStartPostponedEnterTransition();
                        Log.e(GLIDE, "Data Binding image loading failed (from filepath)", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ((FragmentActivity)context).supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(view);
    }
}
