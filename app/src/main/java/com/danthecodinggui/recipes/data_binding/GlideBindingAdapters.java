package com.danthecodinggui.recipes.data_binding;

import android.app.Activity;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Set of methods to be called when specific data binding attributes are set in layout files,
 * specifically those related to Glide image loading.
 */
public class GlideBindingAdapters {

    @BindingAdapter("imageUri")
    public static void setImageResource(final ImageView view, Uri imageUri) {

        //Load an image (local storage) into an ImageView from a Uri

        final Context context = view.getContext();

        Glide.with(context)
                .load(imageUri)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        ((FragmentActivity)context).supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ((FragmentActivity)context).supportStartPostponedEnterTransition();
                        return true;
                    }
                })
                .into(view);
    }
}
