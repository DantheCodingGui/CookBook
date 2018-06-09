package com.danthecodinggui.recipes.model;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for database interaction
 */
final class ProviderContract {

    static final String CONTENT_AUTHORITY = "com.danthecodinggui.recipes.provider";

    //Section of URI identifying table name
    static final String PATH_RECIPES = "Recipes";
    static final String PATH_INGREDIENTS = "Ingredients";
    static final String PATH_METHOD = "Method";
    static final String PATH_RECIPE_INGREDIENTS = "RecipeIngredients";

    //Base uri that all others build on
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Public URIs to access tables
    static final Uri RECIPES_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPES).build();
    static final Uri INGREDIENTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INGREDIENTS).build();
    static final Uri METHOD_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_METHOD).build();
    static final Uri RECIPE_INGREDIENTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECIPE_INGREDIENTS).build();

    //Empty constructor in case anyone instantiates class
    ProviderContract() {}

    /**
     * Core recipe data
     */
    final class RecipeEntry implements BaseColumns {

        static final String TABLE_NAME = "Recipes";

        static final String VIEW_ORDER = "ViewOrder";
        static final String TITLE = "Title";
        static final String CALORIES_PER_PERSON = "Calories";
        static final String DURATION = "Duration";
        static final String IMAGEPATH = "ImagePath";
    }

    /**
     * List of ingredients for a particular recipe
     */
    final class RecipeIngredientEntry implements BaseColumns {

        static final String TABLE_NAME = "RecipeIngredients";

        static final String RECIPE_ID = "RecipeId";
        static final String INGREDIENT_NAME = "IngredientName";
    }

    /**
     * Step in recipe method with associated recipe
     */
    final class MethodStepEntry implements BaseColumns {

        static final String TABLE_NAME = "Steps";

        static final String RECIPE_ID = "Recipe";
        static final String STEP_NO = "StepNo";
        static final String TEXT = "Text";
    }
}
