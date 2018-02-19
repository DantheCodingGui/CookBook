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

    private static DBHelper instance;

    static DBHelper getInstance(Context context) {
        if (instance == null)
            instance = new DBHelper(context);
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.RecipeEntry.CREATE);
        db.execSQL(DBContract.IngredientEntry.CREATE);
        db.execSQL(DBContract.MethodStepEntry.CREATE);
        db.execSQL(DBContract.RecipeIngredientEntry.CREATE);
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
