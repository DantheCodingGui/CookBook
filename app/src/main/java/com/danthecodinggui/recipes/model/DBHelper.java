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
            ModelContract.RecipeEntry.TABLE_NAME + "(" +
            ModelContract.RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ModelContract.RecipeEntry.VIEW_ORDER + "INTEGER NOT NULL, " +
            ModelContract.RecipeEntry.TITLE + " TEXT NOT NULL, " +
            ModelContract.RecipeEntry.CALORIES_PER_PERSON + " INTEGER, " +
            ModelContract.RecipeEntry.DURATION + " INTEGER, " +
            ModelContract.RecipeEntry.IMAGEPATH + " TEXT);";

    private static final String CREATE_INGREDIENT = "CREATE TABLE " +
            ModelContract.IngredientEntry.TABLE_NAME + "(" +
            ModelContract.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ModelContract.IngredientEntry.NAME + " TEXT NOT NULL);";

    private static final String CREATE_METHOD = "CREATE TABLE " +
            ModelContract.MethodStepEntry.TABLE_NAME + "(" +
            ModelContract.MethodStepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ModelContract.MethodStepEntry.RECIPE_ID + " INTEGER NOT NULL, " +
            ModelContract.MethodStepEntry.STEP_NO + " INTEGER NOT NULL, " +
            ModelContract.MethodStepEntry.TEXT + " TEXT NOT NULL, " +
            "FOREIGN KEY(" + ModelContract.MethodStepEntry.RECIPE_ID + ") REFERENCES " +
            ModelContract.RecipeEntry.TABLE_NAME + "(" + ModelContract.RecipeEntry._ID + "));";

    private static final String CREATE_RECIPE_INGREDIENT = "CREATE TABLE " +
            ModelContract.RecipeIngredientEntry.TABLE_NAME + "(" +
            ModelContract.RecipeIngredientEntry. _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ModelContract.RecipeIngredientEntry.RECIPE_ID + " TEXT NOT NULL, " +
            ModelContract.RecipeIngredientEntry.INGREDIENT_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + ModelContract.RecipeIngredientEntry.RECIPE_ID + ") REFERENCES " +
            ModelContract.RecipeEntry.TABLE_NAME + "(" + ModelContract.RecipeEntry._ID + "), " +
            "FOREIGN KEY(" + ModelContract.RecipeIngredientEntry.INGREDIENT_ID + ") REFERENCES " +
            ModelContract.IngredientEntry.TABLE_NAME + "(" + ModelContract.IngredientEntry._ID + "));";

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
        db.execSQL("DROP TABLE IF EXISTS " + ModelContract.RecipeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ModelContract.IngredientEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ModelContract.MethodStepEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ModelContract.RecipeIngredientEntry.TABLE_NAME);
    }
}
