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
        //TODO add creation SQL Statement
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersionNo, int newVersionNo) {
        //TODO actually write this properly this time
    }
}
