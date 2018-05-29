package com.danthecodinggui.recipes.model;

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
        static final String CALORIES_PER_PERSON = "Calories";
        static final String DURATION = "Duration";
        static final String IMAGEPATH = "ImagePath";
    }

    /**
     * List of ingredients for a particular recipe
     */
    final class RecipeIngredientEntry {

        static final String TABLE_NAME = "RecipeIngredients";

        static final String RECIPE_INGREDIENT_ID = "_id";
        static final String RECIPE_ID = "Recipe";
        static final String INGREDIENT_ID = "Ingredient";
    }

    /**
     * List of currently stored ingredients
     */
    final class IngredientEntry {

        static final String TABLE_NAME = "Ingredients";

        static final String INGREDIENT_ID = "_id";
        static final String NAME = "Name";
    }

    /**
     * Step in recipe method with associated recipe
     */
    final class MethodStepEntry {

        static final String TABLE_NAME = "Steps";

        static final String METHOD_STEP_ID = "_id";
        static final String RECIPE_ID = "Recipe";
        static final String STEP_NO = "StepNo";
        static final String TEXT = "Text";
    }
}
