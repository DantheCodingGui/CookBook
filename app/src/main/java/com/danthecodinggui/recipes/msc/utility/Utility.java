package com.danthecodinggui.recipes.msc.utility;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import java.util.Collections;
import java.util.List;

/**
 * Set of generic utility methods
 */
public class Utility {

    public static boolean isRightToLeft(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Get a particular color in a theme
     * @param srcActivity The activity that the theme is applied to
     * @param id The name of the color attribute to get
     * @return The color requested
     */
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
     * Verifies version of operating system, so can use features post-nougat such as split screen
     */
    public static boolean isMultiWindow(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }

    public static int dpToPx(int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * Set the visibility of the keyboard programmatically
     */
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

    /**
     * Set the visibility of the keyboard programmatically
     */
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
     * @param list List that the RecyclerView is bound to
     * @param fromPosition Initial position
     * @param toPosition New position
     * @param <T> List with moved element
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
