package com.danthecodinggui.recipes.model;

import android.provider.BaseColumns;

final class DBSchema {

    static final String INGREDIENTS_JOIN = "(SELECT " +
            RecipeIngredientEntry.TABLE_NAME + "." + RecipeIngredientEntry._ID + ", " +
            RecipeIngredientEntry.RECIPE_ID + ", " + IngredientEntry.NAME + " FROM " +
            RecipeIngredientEntry.TABLE_NAME + " INNER JOIN " + IngredientEntry.TABLE_NAME +
            " ON " + RecipeIngredientEntry.INGREDIENT_ID  + " = " + IngredientEntry.TABLE_NAME +
            "." + IngredientEntry._ID + ")";

    /**
     * Core recipe data
     */
    final class RecipeEntry implements BaseColumns {

        static final String TABLE_NAME = "Recipes";

        static final String VIEW_ORDER = "ViewOrder";
        static final String TITLE = "Title";
        static final String CALORIES_PER_PERSON = "Calories";
        static final String DURATION = "Duration";
        static final String IMAGE_PATH = "ImagePath";
    }

    /**
     * List of ingredients for a particular recipe
     */
    final class RecipeIngredientEntry implements BaseColumns {

        static final String TABLE_NAME = "RecipeIngredients";

        static final String RECIPE_ID = "RecipeId";
        static final String INGREDIENT_ID = "IngredientId";
    }

    /**
     * List of all stored ingredients
     */
    final class IngredientEntry implements BaseColumns {

        static final String TABLE_NAME = "Ingredients";

        static final String NAME = "IngredientName";
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
