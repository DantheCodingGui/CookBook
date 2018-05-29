package com.danthecodinggui.recipes.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Basic Database helper to create and handle core database changes
 */
class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "recipes.db";

    private static final String CREATE_RECIPE = "CREATE TABLE " +
            DBContract.RecipeEntry.TABLE_NAME + "(" +
            DBContract.RecipeEntry.RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.RecipeEntry.VIEW_ORDER + "INTEGER NOT NULL, " +
            DBContract.RecipeEntry.TITLE + " TEXT NOT NULL, " +
            DBContract.RecipeEntry.CALORIES_PER_PERSON + " INTEGER, " +
            DBContract.RecipeEntry.DURATION + " INTEGER, " +
            DBContract.RecipeEntry.IMAGEPATH + " TEXT);";

    private static final String CREATE_INGREDIENT = "CREATE TABLE " +
            DBContract.IngredientEntry.TABLE_NAME + "(" +
            DBContract.IngredientEntry.INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.IngredientEntry.NAME + " TEXT NOT NULL);";

    private static final String CREATE_METHOD = "CREATE TABLE " +
            DBContract.MethodStepEntry.TABLE_NAME + "(" +
            DBContract.MethodStepEntry.METHOD_STEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.MethodStepEntry.RECIPE_ID + " INTEGER NOT NULL, " +
            DBContract.MethodStepEntry.STEP_NO + " INTEGER NOT NULL, " +
            DBContract.MethodStepEntry.TEXT + " TEXT NOT NULL, " +
            "FOREIGN KEY(" + DBContract.MethodStepEntry.RECIPE_ID + ") REFERENCES " +
            DBContract.RecipeEntry.TABLE_NAME + "(" + DBContract.RecipeEntry.RECIPE_ID + "));";

    private static final String CREATE_RECIPE_INGREDIENT = "CREATE TABLE " +
            DBContract.RecipeIngredientEntry.TABLE_NAME + "(" +
            DBContract.RecipeIngredientEntry. RECIPE_INGREDIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBContract.RecipeIngredientEntry.RECIPE_ID + " TEXT NOT NULL, " +
            DBContract.RecipeIngredientEntry.INGREDIENT_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + DBContract.RecipeIngredientEntry.RECIPE_ID + ") REFERENCES " +
            DBContract.RecipeEntry.TABLE_NAME + "(" + DBContract.RecipeEntry.RECIPE_ID + "), " +
            "FOREIGN KEY(" + DBContract.RecipeIngredientEntry.INGREDIENT_ID + ") REFERENCES " +
            DBContract.IngredientEntry.TABLE_NAME + "(" + DBContract.IngredientEntry.INGREDIENT_ID + "));";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECIPE);
        db.execSQL(CREATE_INGREDIENT);
        db.execSQL(CREATE_METHOD);
        db.execSQL(CREATE_RECIPE_INGREDIENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersionNo, int newVersionNo) {
        //TODO actually write this properly this time
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.RecipeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.IngredientEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.MethodStepEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.RecipeIngredientEntry.TABLE_NAME);
    }
}
