package com.danthecodinggui.recipes.msc;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;

import java.io.File;
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
     * Shows permission request rationale dialog, if a user has denied a permission and needs an
     * explanation on why it is needed.
     * @param message The explanation on why the permission is required
     * @param snackbarAnchor The view that a snackbar can be anchored to, to alert user where
     *                       permission settings can be altered
     * @param permission The permission requested
     * @param permissionRequestCode The request-specific code for this query
     * @param callback Deals with any code needing to be run if a feature is being disabled as part of
     *                 denying the permission
     */
    public static void showPermissionDeniedDialog(final Context context, String message,
                                                  final View snackbarAnchor,
                                                  final String permission,
                                                  final int permissionRequestCode,
                                                  final PermissionDialogListener callback) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton(R.string.perm_dialog_butt_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (callback != null)
                            callback.onFeatureDisabled();
                        showPermissionReenableSnackbar(snackbarAnchor, permission);
                    }
                })
                .setPositiveButton(R.string.perm_dialog_butt_permit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        PermissionsHandler.AskForPermission(context, permission,
                                permissionRequestCode, true);
                    }
                })
                .create();

        dialog.show();
    }

    /**
     * Method using string resource rather than string
     * @see #showPermissionDeniedDialog(Context, String, View, String, int, PermissionDialogListener)
     */
    public static void showPermissionDeniedDialog(final Context context, int stringResource,
                                                  final View snackbarAnchor,
                                                  final String permissionCode,
                                                  final int permissionRequestCode,
                                                  final PermissionDialogListener callback) {
        showPermissionDeniedDialog(context, context.getResources().getString(stringResource),
                snackbarAnchor, permissionCode, permissionRequestCode, callback);
    }

    /**
     * Display snackbar informing users where they can change the application's permission settings
     * @param snackbarAnchor The view that a snackbar can be anchored to, to alert user where
     *                       permission settings can be altered
     * @param permissionName The name of the permission you are referencing
     */
    public static void showPermissionReenableSnackbar(View snackbarAnchor, String permissionName) {
        Context context = snackbarAnchor.getContext();

        String text = context.getResources().getString(R.string.perm_reenable_snackbar_msg, permissionName);
        Snackbar.make(snackbarAnchor, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = view.getContext();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                })
                .setActionTextColor(context.getResources().getColor(android.R.color.holo_blue_light))
                .show();
    }

    /**
     * Display snackbar informing users they can reenable permissions in settings
     * @param snackbarAnchor The view that a snackbar can be anchored to, to alert user where
     *                       permission settings can be altered
     */
    public static void showPermissionDeniedSnackbar(View snackbarAnchor) {
        Context context = snackbarAnchor.getContext();

        String text = context.getResources().getString(R.string.perm_denied_snackbar_msg);
        Snackbar.make(snackbarAnchor, text, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Test method to insert dummy recipe, used until AddRecipeActivity is functional
     * @param imagePath
     */
    public static void InsertValue(Context context, String imagePath, boolean image, boolean complex, int viewOrder) {

        //TODO remove later

        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();

        values.put(ProviderContract.RecipeEntry.VIEW_ORDER, viewOrder);
        values.put(ProviderContract.RecipeEntry.TITLE, "Pasta Aglio E Olio");
        if (complex) {
            values.put(ProviderContract.RecipeEntry.CALORIES_PER_PERSON, 340);
            values.put(ProviderContract.RecipeEntry.DURATION, 20);
        }
        if (image)
            values.put(ProviderContract.RecipeEntry.IMAGE_PATH, imagePath);

        Uri result = resolver.insert(
                ProviderContract.RECIPES_URI,
                values);

        long recipeId = ContentUris.parseId(result);

        //Ingredients
        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Spaghetti");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Garlic");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Parsley");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Olive Oil");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Red Pepper Flake");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Chicken (Optional)");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        //Method

        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 1);
        values.put(ProviderContract.MethodStepEntry.STEP_TEXT, "Gradually heat up oil in pan and saute garlic until golden");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 2);
        values.put(ProviderContract.MethodStepEntry.STEP_TEXT, "Add Red Pepper Flake and chopped Parsley");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);


        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 3);
        values.put(ProviderContract.MethodStepEntry.STEP_TEXT, "Toss with cooked spaghetti and add_activity_toolbar cooked chicken if desired");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);
    }

    /**
     * Verifies version of operating system, so can use features post-nougat such as split screen
     */
    public static boolean isMultiWindow(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode();
    }

    public static int dpToPx(Context context, int dp) {
        Resources res = context.getResources();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static void setKeyboardVisibility(Context context, View view, boolean shouldShow) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (shouldShow)
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        else
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public static boolean DeleteFile(String path) {

        if (path == null)
            return false;

        File imageFile = new File(path);
        if (imageFile.exists())
            return imageFile.delete();
        return false;
    }

    public static void ClearDir(String dirPath) {

        if (dirPath == null)
            return;

        File dir = new File(dirPath);

        if (dir.isDirectory())
            for (File child: dir.listFiles())
                child.delete();
    }

    public static boolean CreateDir(String path) {

        if (path == null)
            return false;

        File dir = new File(path);
        if (!dir.exists())
            return dir.mkdir();
        return false;
    }

    public static boolean imageExists(String imagePath) {
        if (Patterns.WEB_URL.matcher(imagePath).matches())
            return true;
        return isImageLocal(imagePath);
    }

    public static boolean isImageLocal(String filepath) {
        return new File(filepath).exists();
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

    public interface PermissionDialogListener {
        /**
         * When user has verified that they have denied a permission, this method should handle
         * disabling any functionality that the permission was needed for
         */
        void onFeatureDisabled();
    }
}
