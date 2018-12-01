package com.danthecodinggui.recipes.msc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.danthecodinggui.recipes.R;

import static com.danthecodinggui.recipes.msc.LogTags.PERMISSIONS;

/**
 * Reduces some boilerplate code concerning runtime permissions
 */
public class PermissionsHandler{

    /*
     * Check if version is Marshmallow and above. Decides whether we need to ask for runtime
     * permissions
     * */
    private static final boolean runtimePermissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    //Permission that this application should need
    private static final String[] APPLICATION_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    //Permission request return codes
    public static final int PERMISSION_REQUESTING = 0;
    public static final int PERMISSION_GRANTED = 1;
    public static final int PERMISSION_DENIED = 2;

    /**
     *
     * @param permissions The permissions being requested
     * @param requestId A unique id for the request to be used in identifying the request later
     * @param rationaleText Text to be displayed in the event of a rational dialog being shown
     * @return The response code, being one of: <br/>
     * <ul>
     *     <li><code>PERMISSION_REQUESTING</code> - The permission has not been granted yet, but now
     *         a permission request bas been filed. Override the Activities <code>onRequestPermissionsResult()</code>
     *         method to see the asynchronous result.</li>
     *     <li><code>PERMISSION_GRANTED</code> - The permission has already been granted.
     *     The caller is allowed to run the code requiring this permission.</li>
     *     <li><code>PERMISSION_DENIED</code> - The user has previously denied the permission.
     *     Explanation dialog will be automatically shown if rationalText provided.</li>
     * </ul>
     *
     */
    public static int AskForPermission(Context context, String[] permissions, int requestId, String rationaleText) {

        if (context == null || permissions == null)
            return PERMISSION_DENIED;

        Activity srcActivity = (Activity) context;

        for (String perm: permissions) {
            Log.d(PERMISSIONS, "Permission " + perm + " requested by " + srcActivity.getLocalClassName());

            //Check to ensure that requested permission is one in which the application should be using
            boolean isValidPermission = false;
            for (String p : APPLICATION_PERMISSIONS) {
                if (p.equals(perm))
                    isValidPermission = true;
            }
            if (!isValidPermission)
                throw new InvalidPermissionException("Requested permission is not in the manifest");
        }

        //If pre-runtime permissions, we can just assume that all permissions are granted
        if (!runtimePermissions)
            return PERMISSION_GRANTED;

        //Does the app already have the requested permission??
        if (!HasPermissions(context, permissions)) {

            for (String perm: permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(srcActivity, perm)) {
                    Log.i(PERMISSIONS, "Permissions previously been denied");
                    if (rationaleText != null && !rationaleText.isEmpty()) {
                        ShowRationaleDialog(srcActivity, permissions, requestId, rationaleText);
                        return PERMISSION_DENIED;
                    }
                }
            }

            ActivityCompat.requestPermissions(srcActivity, permissions, requestId);
            Log.i(PERMISSIONS, "Permissions being granted");
            return PERMISSION_REQUESTING;
        }
        else {
            Log.i(PERMISSIONS, "Permissions already granted");
            return PERMISSION_GRANTED;
        }
    }

    /**
     * Shows permission request rationale dialog, if a user repeatedly tries to access functionality
     * and repeatedly denies permission
     * @param activity The activity in which the dialog will be displayed
     * @param permissions The permissions being requested
     * @param requestId A unique id for the request to be used in identifying the request later
     * @param rationaleText The text explanation to be displayed in the dialog
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void ShowRationaleDialog(final Activity activity, String[] permissions, int requestId, String rationaleText) {
        new AlertDialog.Builder(activity)
                .setMessage(rationaleText)
                .setPositiveButton(R.string.perm_dialog_butt_permit, (dialog, i) -> {
                    Log.i(PERMISSIONS, "Permissions being asked again");
                    ActivityCompat.requestPermissions(activity, permissions, requestId);
                })
                .setNegativeButton(R.string.perm_dialog_butt_deny, (dialog, i) -> {})
                .create()
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean HasPermissions(Context context, String[] permissions) {
        for (String perm: permissions) {
            if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    /**
     *
     * @param permissions The permissions being requested
     * @param requestId A unique id for the request to be used in identifying the request later
     * @return The response code, being one of: <br/>
     * <ul>
     *     <li><code>PERMISSION_REQUESTING</code> - The permission has not been granted yet, but now
     *         a permission request bas been filed. Override the Activities <code>onRequestPermissionsResult()</code>
     *         method to see the asynchronous result.</li>
     *     <li><code>PERMISSION_GRANTED</code> - The permission has already been granted.
     *     The caller is allowed to run the code requiring this permission.</li>
     *     <li><code>PERMISSION_DENIED</code> - The user has previously denied the permission.
     *     If intent not obvious then present some kind of explanation dialogue (async) before asking again.</li>
     * </ul>
     *
     */
    public static int AskForPermission(Context context, String[] permissions, int requestId) {
        return AskForPermission(context, permissions, requestId, null);
    }

    /**
     *
     * @param permission The permission being requested
     * @param requestId A unique id for the request to be used in identifying the request later
     * @param rationaleText Text to be displayed in the event of a rational dialog being shown
     * @return The response code, being one of: <br/>
     * <ul>
     *     <li><code>PERMISSION_REQUESTING</code> - The permission has not been granted yet, but now
     *         a permission request bas been filed. Override the Activities <code>onRequestPermissionsResult()</code>
     *         method to see the asynchronous result.</li>
     *     <li><code>PERMISSION_GRANTED</code> - The permission has already been granted.
     *     The caller is allowed to run the code requiring this permission.</li>
     *     <li><code>PERMISSION_DENIED</code> - The user has previously denied the permission.
     *     If intent not obvious then present some kind of explanation dialogue (async) before asking again.</li>
     * </ul>
     *
     */
    public static int AskForPermission(Context context, String permission, int requestId, String rationaleText) {
        return AskForPermission(context, new String[] { permission }, requestId, rationaleText);
    }

    /**
     *
     * @param permission The permission being requested
     * @param requestId A unique id for the request to be used in identifying the request later
     * @return The response code, being one of: <br/>
     * <ul>
     *     <li><code>PERMISSION_REQUESTING</code> - The permission has not been granted yet, but now
     *         a permission request bas been filed. Override the Activities <code>onRequestPermissionsResult()</code>
     *         method to see the asynchronous result.</li>
     *     <li><code>PERMISSION_GRANTED</code> - The permission has already been granted.
     *     The caller is allowed to run the code requiring this permission.</li>
     *     <li><code>PERMISSION_DENIED</code> - The user has previously denied the permission.
     *     If intent not obvious then present some kind of explanation dialogue (async) before asking again.</li>
     * </ul>
     *
     */
    public static int AskForPermission(Context context, String permission, int requestId) {
        return AskForPermission(context, new String[] { permission }, requestId, null);
    }

    private static class InvalidPermissionException extends RuntimeException {
        private InvalidPermissionException(String message) {
            super(message);
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