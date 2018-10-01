package com.danthecodinggui.recipes.msc;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.View;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.model.ProviderContract;

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
                        callback.onFeatureDisabled();
                        showPermissionDeniedSnackbar(snackbarAnchor);
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
     */
    public static void showPermissionDeniedSnackbar(View snackbarAnchor) {
        Snackbar.make(snackbarAnchor, R.string.perm_snackbar_msg, Snackbar.LENGTH_LONG)
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
        values.put(ProviderContract.MethodStepEntry.TEXT, "Gradually heat up oil in pan and saute garlic until golden");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 2);
        values.put(ProviderContract.MethodStepEntry.TEXT, "Add Red Pepper Flake and chopped Parsley");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);


        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 3);
        values.put(ProviderContract.MethodStepEntry.TEXT, "Toss with cooked spaghetti and add cooked chicken if desired");
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

    public interface PermissionDialogListener {
        /**
         * When user has verified that they have denied a permission, this method should handle
         * disabling any functionality that the permission was needed for
         */
        void onFeatureDisabled();
    }
}
