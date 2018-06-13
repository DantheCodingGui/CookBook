package com.danthecodinggui.recipes.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Content provider for accessing all application data
 */
public class RecipeProvider extends ContentProvider {

    //URI codes
    private static final int RECIPES_TABLE = 100;
    private static final int RECIPES_ITEM = 101;
    private static final int METHOD_TABLE = 102;
    private static final int METHOD_ITEM = 103;
    private static final int RECIPE_INGREDIENTS_TABLE = 104;
    private static final int RECIPE_INGREDIENTS_ITEM = 105;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, ProviderContract.PATH_RECIPES, RECIPES_TABLE);
        uriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, ProviderContract.PATH_RECIPES + "/#", RECIPES_ITEM);
        uriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, ProviderContract.PATH_METHOD, METHOD_TABLE);
        uriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, ProviderContract.PATH_METHOD + "/#", METHOD_ITEM);
        uriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, ProviderContract.PATH_RECIPE_INGREDIENTS, RECIPE_INGREDIENTS_TABLE);
        uriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, ProviderContract.PATH_RECIPE_INGREDIENTS + "/#", RECIPE_INGREDIENTS_ITEM);
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

        ContentValues editedValues = values;

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                tableName = DBSchema.RecipeEntry.TABLE_NAME;
                break;
            case METHOD_TABLE:
                tableName = DBSchema.MethodStepEntry.TABLE_NAME;
                break;
            case RECIPE_INGREDIENTS_TABLE:
                //Need to handle 1 provider table -> 2 db tables
                long recipeId = values.getAsLong(ProviderContract.RecipeIngredientEntry.RECIPE_ID);
                String ingredient = values.getAsString(
                        ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME);

                long ingredientId = GetIngredientId(ingredient);

                editedValues = new ContentValues();
                values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
                values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, ingredientId);

                tableName = DBSchema.RecipeIngredientEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for insertion: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        long id = db.insert(tableName, null, editedValues);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Checks to see if ingredient for recipe already exists in ingredients table,
     * updates if required
     * @param ingredientName The name of the ingredient to check
     * @return The primary key of the ingredient
     */
    private long GetIngredientId(String ingredientName) {

        //TODO sanitise input here to prevent sql injection

        //Query db to see if ingredient is in table
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] columns = { DBSchema.IngredientEntry._ID };
        String selection = DBSchema.IngredientEntry.NAME + " = ?";
        String[] selectionArgs = { ingredientName };

        Cursor ingredients = db.query(
                DBSchema.IngredientEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
                );

        //Ingredient is not already in table, must add it
        if (ingredients == null) {
            ContentValues newIngredient = new ContentValues();
            newIngredient.put(DBSchema.IngredientEntry.NAME, ingredientName);

            return db.insert(
                    DBSchema.IngredientEntry.TABLE_NAME,
                    null,
                    newIngredient
            );
        }

        //Ingredient is already in table, just return it's ID
        ingredients.moveToFirst();
        return ingredients.getLong(
                ingredients.getColumnIndexOrThrow(DBSchema.IngredientEntry._ID));
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                qBuilder.setTables(DBSchema.RecipeEntry.TABLE_NAME);
                break;
            case RECIPES_ITEM:
                qBuilder.setTables(DBSchema.RecipeEntry.TABLE_NAME);
                //Limit query result to one record
                qBuilder.appendWhere(DBSchema.RecipeEntry._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case METHOD_TABLE:
                qBuilder.setTables(DBSchema.MethodStepEntry.TABLE_NAME);
                break;
            case METHOD_ITEM:
                qBuilder.setTables(DBSchema.MethodStepEntry.TABLE_NAME);
                qBuilder.appendWhere(DBSchema.MethodStepEntry._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case RECIPE_INGREDIENTS_TABLE:
                qBuilder.setTables(DBSchema.INGREDIENTS_JOIN);
                break;
            case RECIPE_INGREDIENTS_ITEM:
                qBuilder.setTables(DBSchema.INGREDIENTS_JOIN);
                qBuilder.appendWhere(DBSchema.RecipeIngredientEntry._ID + " = "
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

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowNumUpdated;
        String id;
        String where;

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                rowNumUpdated = db.update(
                        DBSchema.RecipeEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case RECIPES_ITEM:
                id = uri.getLastPathSegment();
                where = DBSchema.RecipeEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumUpdated = db.update(
                        DBSchema.RecipeEntry.TABLE_NAME,
                        values,
                        where,
                        selectionArgs
                );
                break;
            case METHOD_TABLE:
                rowNumUpdated = db.update(
                        DBSchema.MethodStepEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case METHOD_ITEM:
                id = uri.getLastPathSegment();
                where = DBSchema.MethodStepEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumUpdated = db.update(
                        DBSchema.MethodStepEntry.TABLE_NAME,
                        values,
                        where,
                        selectionArgs
                );
                break;
            case RECIPE_INGREDIENTS_TABLE:
                rowNumUpdated = db.update(
                        DBSchema.IngredientEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case RECIPE_INGREDIENTS_ITEM:
                id = uri.getLastPathSegment();
                where = DBSchema.RecipeIngredientEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumUpdated = db.update(
                        DBSchema.RecipeIngredientEntry.TABLE_NAME,
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

        //Same structure as update with changed var names and db methods

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowNumDeleted;
        String id;
        String where;

        switch (uriMatcher.match(uri)) {
            case RECIPES_TABLE:
                rowNumDeleted = db.delete(
                        DBSchema.RecipeEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case RECIPES_ITEM:
                id = uri.getLastPathSegment();
                where = DBSchema.RecipeEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumDeleted = db.delete(
                        DBSchema.RecipeEntry.TABLE_NAME,
                        where,
                        selectionArgs
                );
                break;
            case METHOD_TABLE:
                rowNumDeleted = db.delete(
                        DBSchema.MethodStepEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case METHOD_ITEM:
                id = uri.getLastPathSegment();
                where = DBSchema.MethodStepEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumDeleted = db.delete(
                        DBSchema.MethodStepEntry.TABLE_NAME,
                        where,
                        selectionArgs
                );
                break;
            case RECIPE_INGREDIENTS_TABLE:
                /* TODO as we're using a view for ingredients, will need to handle references to
                 ingredients when something is deleted, ie. check to see if deleted ingredients
                 are still referenced in recipe-ingredients table
                 */
                rowNumDeleted = db.delete(
                        DBSchema.IngredientEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case RECIPE_INGREDIENTS_ITEM:
                id = uri.getLastPathSegment();
                where = DBSchema.RecipeIngredientEntry._ID + " = " + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowNumDeleted = db.delete(
                        DBSchema.RecipeIngredientEntry.TABLE_NAME,
                        where,
                        selectionArgs
                );
                break;
            default:
                throw new IllegalArgumentException("Invalid URI for deletion: " + uri);
        }

        return rowNumDeleted;

    }
}
