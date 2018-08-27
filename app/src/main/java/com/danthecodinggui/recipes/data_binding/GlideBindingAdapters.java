package com.danthecodinggui.recipes.data_binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Set of methods to be called when specific data binding attributes are set in layout files,
 * specifically those related to Glide image loading.
 */
public class GlideBindingAdapters {

    @BindingAdapter("imageUri")
    public static void setImageResource(ImageView view, Uri imageUri) {

        //Load an image (local storage) into an ImageView from a Uri

        Context context = view.getContext();

        Glide.with(context)
                .load(imageUri)
                .into(view);
    }
}
