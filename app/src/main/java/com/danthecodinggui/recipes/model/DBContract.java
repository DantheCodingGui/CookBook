package com.danthecodinggui.recipes.model;

import android.net.Uri;

/**
 * Contract for database interaction
 */
final class DBContract {

    //Empty constructor in case anyone instantiates class
    DBContract() {}

    /**
     * Core recipe data
     */
    final class RecipeEntry {

        static final String TABLE_NAME = "Recipes";

        static final String RECIPE_ID = "_id";
        static final String VIEW_ORDER = "ViewOrder";
        static final String TITLE = "Title";
        static final String CALORIES = "Calories";
        static final String DURATION = "Duration";
        static final String IMAGEPATH = "ImagePath";

        static final String CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VIEW_ORDER + "INTEGER NOT NULL, " +
                TITLE + " TEXT NOT NULL, " +
                CALORIES + " INTEGER, " +
                DURATION + " INTEGER, " +
                IMAGEPATH + " TEXT);";
    }

    /**
     * List of currently stored ingredients
     */
    final class RecipeIngredientEntry {

        static final String TABLE_NAME = "RecipeIngredients";

        static final String RECIPE_INGREDIENT_ID = "_id";
        static final String RECIPE = "Recipe";
        static final String INGREDIENT = "Ingredient";

        static final String CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                RECIPE_INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RECIPE + " TEXT NOT NULL, " +
                INGREDIENT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + RECIPE + ") REFERENCES " +
                        RecipeEntry.TABLE_NAME + "(" + RecipeEntry.RECIPE_ID + "), " +
                "FOREIGN KEY(" + INGREDIENT + ") REFERENCES " +
                        IngredientEntry.TABLE_NAME + "(" + IngredientEntry.INGREDIENT_ID + ");";
    }

    /**
     * List of currently stored ingredients
     */
    final class IngredientEntry {

        static final String TABLE_NAME = "Ingredients";

        static final String INGREDIENT_ID = "_id";
        static final String NAME = "Name";

        static final String CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT NOT NULL);";
    }

    /**
     * Step in recipe method with associated recipe
     */
    final class MethodStepEntry {

        static final String TABLE_NAME = "Steps";

        static final String METHOD_STEP_ID = "_id";
        static final String RECIPE = "Recipe";
        static final String TEXT = "Text";

        static final String CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                METHOD_STEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RECIPE + " INTEGER NOT NULL, " +
                TEXT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + RECIPE + ") REFERENCES " +
                    RecipeEntry.TABLE_NAME + "(" + RecipeEntry.RECIPE_ID + ");";
    }
}
