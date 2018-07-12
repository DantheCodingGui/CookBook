package com.danthecodinggui.recipes.msc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static com.danthecodinggui.recipes.msc.LogTags.PERMISSIONS;

/**
 * Reduces some boilerplate code concerning runtime permissions <br/>
 */
public class PermissionsHandler{

    /*
     * Check if version is Marshmallow and above. Decides whether we need to ask for runtime
     * permissions
     * */
    private static final boolean runtimePermissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    //Permission strings used in the application
    private static final String[] APPLICATION_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Permission request return codes
    public static final int PERMISSION_GRANTING = 0;
    public static final int PERMISSION_ALREADY_GRANTED = 1;
    public static final int PERMISSION_PREVIOUSLY_DENIED = 2;
    public static final int PERMISSION_INVALID = 3;

    /**
     *
     * @param permission The permission being requested
     * @param requestId A unique id for the request to be used in identifying the request later.
     * @param rationalOverride States whether user has denied a permission, and changed their mind
     *                         after reading the rational dialog
     * @return The response code, being one of: <br/>
     * <ul>
     *     <li><code>PERMISSION_GRANTING</code> - The permission has not been granted yet, but now
     *         a permission request bas been filed. Override the Activities <code>onRequestPermissionsResult()</code>
     *         method to see the asynchronous result.</li>
     *     <li><code>PERMISSION_ALREADY_GRANTED</code> - The permission has already been granted.
     *     The caller is allowed to run the code requiring this permission.</li>
     *     <li><code>PERMISSION_PREVIOUSLY_DENIED</code> - The user has previously denied the permission.
     *     If intent not obvious then present some kind of explanation dialogue (async) before asking again.</li>
     *     <li><code>PERMISSION_INVALID</code> - The requested permission is not one that the application
     *     should need, and therefore is disallowed.</li>
     * </ul>
     *
     */
    public static int AskForPermission(Context context, String permission, int requestId,
                                       boolean rationalOverride) {

        Activity srcActivity = (Activity) context;

        Log.d(PERMISSIONS, "Permission " + permission + " requested by " + srcActivity.getLocalClassName());

        //Check to ensure that requested permission is one in which the application should be using
        boolean isValidPermission = false;
        for (String p : APPLICATION_PERMISSIONS) {
            if (p.equals(permission))
                isValidPermission = true;
        }
        if (!isValidPermission) {
            Log.e(PERMISSIONS, "Permission " + permission + " is invalid");
            return PERMISSION_INVALID;
        }

        //Precaution to avoid unnecessary checks if not needed
        if (!runtimePermissions)
            return PERMISSION_ALREADY_GRANTED;

        //Does the app already have the requested permission??
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(srcActivity, permission)
                    && !rationalOverride) {
                Log.i(PERMISSIONS, "Permission " + permission + " previously been denied");
                return PERMISSION_PREVIOUSLY_DENIED;
            }
            else {
                ActivityCompat.requestPermissions(srcActivity, new String[]{permission}, requestId);
                Log.i(PERMISSIONS, "Permission " + permission + " being granted");
                return PERMISSION_GRANTING;
            }
        }
        else {
            Log.i(PERMISSIONS, "Permission " + permission + " already granted");
            return PERMISSION_ALREADY_GRANTED;
        }
    }
}