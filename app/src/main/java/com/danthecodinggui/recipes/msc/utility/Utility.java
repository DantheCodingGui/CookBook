package com.danthecodinggui.recipes.msc.utility;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import java.util.Collections;
import java.util.List;

/**
 * Set of miscellaneous methods without a link to any other class
 */
public class Utility {

    public static boolean isRightToLeft(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static int getThemeColor(Activity srcActivity, int id) {
        Resources.Theme theme = srcActivity.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }

    /**
     * Verifies version of operating system, so can use features post-lollipop such as material
     * design, animations and transitions
     */
    public static boolean atLeastLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Test method to insert dummy recipe, used until AddEditRecipeActivity is functional
     * @param imagePath
     */
    public static void InsertValue(Context context, String imagePath, boolean image, boolean complex) {

//        //TODO remove later
//
//        ContentResolver resolver = context.getContentResolver();
//
//        ContentValues values = new ContentValues();
//
//        values.put(ProviderContract.RecipeEntry.TITLE, "Pasta Aglio E Olio");
//        if (complex) {
//            values.put(ProviderContract.RecipeEntry.CALORIES_PER_PERSON, 340);
//            values.put(ProviderContract.RecipeEntry.DURATION, 20);
//        }
//        if (image)
//            values.put(ProviderContract.RecipeEntry.IMAGE_PATH, imagePath);
//
//        Uri result = resolver.insert(
//                ProviderContract.RECIPES_URI,
//                values);
//
//        long recipeId = ContentUris.parseId(result);
//
//        //Ingredients
//        values = new ContentValues();
//        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Spaghetti");
//        resolver.insert(
//                ProviderContract.RECIPE_INGREDIENTS_URI,
//                values);
//
//        values = new ContentValues();
//        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Garlic");
//        resolver.insert(
//                ProviderContract.RECIPE_INGREDIENTS_URI,
//                values);
//
//        values = new ContentValues();
//        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Parsley");
//        resolver.insert(
//                ProviderContract.RECIPE_INGREDIENTS_URI,
//                values);
//
//        values = new ContentValues();
//        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Olive Oil");
//        resolver.insert(
//                ProviderContract.RECIPE_INGREDIENTS_URI,
//                values);
//
//        values = new ContentValues();
//        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Red Pepper Flake");
//        resolver.insert(
//                ProviderContract.RECIPE_INGREDIENTS_URI,
//                values);
//
//        values = new ContentValues();
//        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Chicken (Optional)");
//        resolver.insert(
//                ProviderContract.RECIPE_INGREDIENTS_URI,
//                values);
//
//        //Method
//
//        values = new ContentValues();
//        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.MethodStepEntry.STEP_NO, 1);
//        values.put(ProviderContract.MethodStepEntry.STEP_TEXT, "Gradually heat up oil in pan and saute garlic until golden");
//        resolver.insert(
//                ProviderContract.METHOD_URI,
//                values);
//
//        values = new ContentValues();
//        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.MethodStepEntry.STEP_NO, 2);
//        values.put(ProviderContract.MethodStepEntry.STEP_TEXT, "Add Red Pepper Flake and chopped Parsley");
//        resolver.insert(
//                ProviderContract.METHOD_URI,
//                values);
//
//
//        values = new ContentValues();
//        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
//        values.put(ProviderContract.MethodStepEntry.STEP_NO, 3);
//        values.put(ProviderContract.MethodStepEntry.STEP_TEXT, "Toss with cooked spaghetti and add_activity_toolbar cooked chicken if desired");
//        resolver.insert(
//                ProviderContract.METHOD_URI,
//                values);
    }

    /**
     * Verifies version of operating system, so can use features post-nougat such as split screen
     */
    public static boolean isMultiWindow(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }

    public static int dpToPx(int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    public static void setKeyboardVisibility(Activity activity, boolean shouldShow) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (shouldShow)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        else {
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void setKeyboardVisibility(Context context, View view, boolean shouldShow) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (shouldShow) {
            view.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    public static boolean isStringAllWhitespace(String s) {
        return s.trim().length() == 0;
    }

    /**
     * Sets a button's enable state based on the state of an associated EditText
     * @param currentText Current EditText value
     */
    public static void CheckButtonEnabled(View button, String currentText) {
        if (currentText.isEmpty())
            button.setEnabled(false);
        else
            button.setEnabled(true);
    }

    /**
     * Interpolate between two colours
     * @param bAmount Percentage between the two to go
     * @return Interpolated colour
     */
    public static int interpolateRGB(final int colorA, final int colorB, final float bAmount) {
        final float aAmount = 1.0f - bAmount;
        final int red = (int) (Color.red(colorA) * aAmount + Color.red(colorB) * bAmount);
        final int green = (int) (Color.green(colorA) * aAmount + Color.green(colorB) * bAmount);
        final int blue = (int) (Color.blue(colorA) * aAmount + Color.blue(colorB) * bAmount);
        return Color.rgb(red, green, blue);
    }

    /**
     * Decides what type of Recipe to build based on what data is saved in the table
     * record
     * @return The base Recipe constructed with the data found in the record
     */
    public static Recipe BuildModelFromCursor(Cursor cursor) {

        //Constant values, always used in constructor
        long pk = cursor.getLong(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry._ID));
        String recipeTitle = cursor.getString(cursor.getColumnIndexOrThrow(
                ProviderContract.RecipeEntry.TITLE));

        int calories = 0;
        int timeInMins = 0;
        String photoPath = null;

        //If optional values exist assign to variables
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.CALORIES_PER_PERSON)))
            calories = cursor.getInt(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.CALORIES_PER_PERSON));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.DURATION)))
            timeInMins = cursor.getInt(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.DURATION));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(ProviderContract.RecipeEntry.IMAGE_PATH)))
            photoPath = cursor.getString(cursor.getColumnIndexOrThrow(
                    ProviderContract.RecipeEntry.IMAGE_PATH));

        return new Recipe.RecipeBuilder(pk, recipeTitle)
                .calories(calories)
                .timeInMins(timeInMins)
                .imageFilePath(photoPath)
                .build();
    }

    /**
     * Update RecyclerView item positions when dragged
     * @param list
     * @param fromPosition
     * @param toPosition
     * @param <T>
     * @return Has the item moved down the list
     */
    public static <T> boolean onRecyclerViewItemMoved(List<T> list, int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(list, i, i + 1);
            return true;
        } else {
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(list, i, i - 1);
            return false;
        }
    }
}
