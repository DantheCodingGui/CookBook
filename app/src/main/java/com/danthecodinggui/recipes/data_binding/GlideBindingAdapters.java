package com.danthecodinggui.recipes.data_binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GlideBindingAdapters {

    @BindingAdapter("imageUri")
    public static void setImageResource(ImageView view, Uri imageUri) {

        Context context = view.getContext();

        Glide.with(context)
                .load(imageUri)
                .into(view);
    }
}
