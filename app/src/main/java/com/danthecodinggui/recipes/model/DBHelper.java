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
            DBSchema.RecipeEntry.TABLE_NAME + "(" +
            DBSchema.RecipeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBSchema.RecipeEntry.VIEW_ORDER + "INTEGER NOT NULL, " +
            DBSchema.RecipeEntry.TITLE + " TEXT NOT NULL, " +
            DBSchema.RecipeEntry.CALORIES_PER_PERSON + " INTEGER, " +
            DBSchema.RecipeEntry.DURATION + " INTEGER, " +
            DBSchema.RecipeEntry.IMAGE_PATH + " TEXT);";

    private static final String CREATE_INGREDIENT = "CREATE TABLE " +
            DBSchema.IngredientEntry.TABLE_NAME + "(" +
            DBSchema.IngredientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBSchema.IngredientEntry.NAME + " TEXT NOT NULL);";

    private static final String CREATE_METHOD = "CREATE TABLE " +
            DBSchema.MethodStepEntry.TABLE_NAME + "(" +
            DBSchema.MethodStepEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBSchema.MethodStepEntry.RECIPE_ID + " INTEGER NOT NULL, " +
            DBSchema.MethodStepEntry.STEP_NO + " INTEGER NOT NULL, " +
            DBSchema.MethodStepEntry.TEXT + " TEXT NOT NULL, " +
            "FOREIGN KEY(" + DBSchema.MethodStepEntry.RECIPE_ID + ") REFERENCES " +
            DBSchema.RecipeEntry.TABLE_NAME + "(" + DBSchema.RecipeEntry._ID + "));";

    private static final String CREATE_RECIPE_INGREDIENT = "CREATE TABLE " +
            DBSchema.RecipeIngredientEntry.TABLE_NAME + "(" +
            DBSchema.RecipeIngredientEntry. _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DBSchema.RecipeIngredientEntry.RECIPE_ID + " TEXT NOT NULL, " +
            DBSchema.RecipeIngredientEntry.INGREDIENT_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + DBSchema.RecipeIngredientEntry.RECIPE_ID + ") REFERENCES " +
            DBSchema.RecipeEntry.TABLE_NAME + "(" + DBSchema.RecipeEntry._ID + "), " +
            "FOREIGN KEY(" + DBSchema.RecipeIngredientEntry.INGREDIENT_ID + ") REFERENCES " +
            DBSchema.IngredientEntry.TABLE_NAME + "(" + DBSchema.IngredientEntry._ID + "));";

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
        db.execSQL("DROP TABLE IF EXISTS " + DBSchema.RecipeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBSchema.IngredientEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBSchema.MethodStepEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBSchema.RecipeIngredientEntry.TABLE_NAME);
    }
}
