package com.danthecodinggui.recipes.model;

import android.net.Uri;

/**
 * Contract for content provider interaction
 */
public class ProviderContract {

    static final String CONTENT_AUTHORITY = "com.danthecodinggui.recipes.provider";

    static final String PATH_RECIPES = "Recipes";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();
}
