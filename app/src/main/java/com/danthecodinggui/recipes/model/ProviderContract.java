package com.danthecodinggui.recipes.model;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for database interaction
 */
public final class ProviderContract {

    static final String CONTENT_AUTHORITY = "com.danthecodinggui.recipes.model.RecipeProvider";

    //Section of URI identifying table name
    static final String PATH_RECIPES = "Recipes";
    static final String PATH_INGREDIENTS = "Ingredients";
    static final String PATH_METHOD = "Method";
    static final String PATH_RECIPE_INGREDIENTS = "RecipeIngredients";

    //Base uri that all others build on
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Public URIs to access tables
    public static final Uri RECIPES_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();
    public static final Uri INGREDIENTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INGREDIENTS).build();
    public static final Uri METHOD_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_METHOD).build();
    public static final Uri RECIPE_INGREDIENTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE_INGREDIENTS).build();

    //Count records projection string
    public static final String[] COUNT_INGREDIENTS_PROJECTION = { "count(" + RecipeIngredientEntry.RECIPE_ID +
            ") AS " + BaseColumns._COUNT};
    public static final String[] COUNT_STEPS_PROJECTION = { "count(" + MethodStepEntry.RECIPE_ID +
            ") AS " + BaseColumns._COUNT};

    //Empty constructor in case anyone instantiates class
    ProviderContract() {}

    /**
     * Core recipe data
     */
    public final class RecipeEntry implements BaseColumns {

        public static final String VIEW_ORDER = "ViewOrder";
        public static final String TITLE = "Title";
        public static final String CALORIES_PER_PERSON = "Calories";
        public static final String DURATION = "Duration";
        public static final String IMAGE_PATH = "ImagePath";
    }

    /**
     * List of ingredients for a particular recipe
     */
    public final class RecipeIngredientEntry implements BaseColumns {

        public static final String RECIPE_ID = "RecipeId";
        public static final String INGREDIENT_NAME = "IngredientName";
    }

    /**
     * Step in recipe method with associated recipe
     */
    public final class MethodStepEntry implements BaseColumns {

        public static final String RECIPE_ID = "Recipe";
        public static final String STEP_NO = "StepNo";
        public static final String TEXT = "Text";
    }
}
