package com.danthecodinggui.recipes.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Content provider for accessing all application data
 */
public class RecipeProvider extends ContentProvider {

    //URI codes
    private static final int RECIPES_TABLE = 100;
    private static final int RECIPES_ITEM = 101;
    private static final int INGREDIENTS_TABLE = 102;
    private static final int INGREDIENTS_ITEM = 103;
    private static final int METHOD_TABLE = 104;
    private static final int METHOD_ITEM = 105;
    private static final int RECIPE_INGREDIENTS_TABLE = 106;
    private static final int RECIPE_INGREDIENTS_ITEM = 107;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_RECIPES, RECIPES_TABLE);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_RECIPES + "/#", RECIPES_ITEM);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_INGREDIENTS, INGREDIENTS_TABLE);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_INGREDIENTS + "/#", INGREDIENTS_ITEM);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_METHOD, METHOD_TABLE);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_METHOD + "/#", METHOD_ITEM);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_RECIPE_INGREDIENTS, RECIPE_INGREDIENTS_TABLE);
        uriMatcher.addURI(ModelContract.CONTENT_AUTHORITY, ModelContract.PATH_RECIPE_INGREDIENTS + "/#", RECIPE_INGREDIENTS_ITEM);
    }

    private DBHelper dbHelper;
    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment() == null)
            return "vnd.android.cursor.dir/RecipeProvider.data.text";
        else
            return "vnd.android.cursor.item/RecipeProvider.data.text";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db;
        String tableName;

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                tableName = ModelContract.RecipeEntry.TABLE_NAME;
                break;
            case INGREDIENTS_TABLE:
                tableName = ModelContract.IngredientEntry.TABLE_NAME;
                break;
            case METHOD_TABLE:
                tableName = ModelContract.MethodStepEntry.TABLE_NAME;
                break;
            case RECIPE_INGREDIENTS_TABLE:
                tableName = ModelContract.RecipeIngredientEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for insertion: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        long id = db.insert(tableName, null, values);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                qBuilder.setTables(ModelContract.RecipeEntry.TABLE_NAME);
                break;
            case RECIPES_ITEM:
                qBuilder.setTables(ModelContract.RecipeEntry.TABLE_NAME);
                //Limit query result to one record
                qBuilder.appendWhere(ModelContract.RecipeEntry._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case INGREDIENTS_TABLE:
                qBuilder.setTables(ModelContract.IngredientEntry.TABLE_NAME);
                break;
            case INGREDIENTS_ITEM:
                qBuilder.setTables(ModelContract.IngredientEntry.TABLE_NAME);
                qBuilder.appendWhere(ModelContract.IngredientEntry._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case METHOD_TABLE:
                qBuilder.setTables(ModelContract.MethodStepEntry.TABLE_NAME);
                break;
            case METHOD_ITEM:
                qBuilder.setTables(ModelContract.MethodStepEntry.TABLE_NAME);
                qBuilder.appendWhere(ModelContract.MethodStepEntry._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case RECIPE_INGREDIENTS_TABLE:
                qBuilder.setTables(ModelContract.RecipeIngredientEntry.TABLE_NAME);
                break;
            case RECIPE_INGREDIENTS_ITEM:
                qBuilder.setTables(ModelContract.RecipeIngredientEntry.TABLE_NAME);
                qBuilder.appendWhere(ModelContract.RecipeIngredientEntry._ID + " = "
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for query: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return qBuilder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowNumUpdated = 0;
        String id;
        String where;

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                rowNumUpdated = db.update(
                        ModelContract.RecipeEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case RECIPES_ITEM:
                id = uri.getLastPathSegment();
                where = ModelContract.RecipeEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumUpdated = db.update(
                        ModelContract.RecipeEntry.TABLE_NAME,
                        values,
                        where,
                        selectionArgs
                );
                break;
            case METHOD_TABLE:
                rowNumUpdated = db.update(
                        ModelContract.MethodStepEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case METHOD_ITEM:
                id = uri.getLastPathSegment();
                where = ModelContract.MethodStepEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumUpdated = db.update(
                        ModelContract.MethodStepEntry.TABLE_NAME,
                        values,
                        where,
                        selectionArgs
                );
                break;
            case RECIPE_INGREDIENTS_TABLE:
                rowNumUpdated = db.update(
                        ModelContract.IngredientEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case RECIPE_INGREDIENTS_ITEM:
                id = uri.getLastPathSegment();
                where = ModelContract.RecipeIngredientEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumUpdated = db.update(
                        ModelContract.RecipeIngredientEntry.TABLE_NAME,
                        values,
                        where,
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for updating: " + uri);
        }

        return rowNumUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
