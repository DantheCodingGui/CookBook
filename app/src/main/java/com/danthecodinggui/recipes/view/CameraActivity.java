package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityCameraBinding;
import com.danthecodinggui.recipes.msc.Utility;

import java.io.File;
import java.security.MessageDigest;
import java.util.UUID;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.selector.LensPositionSelectorsKt;
import io.fotoapparat.selector.ResolutionSelectorsKt;


import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static com.danthecodinggui.recipes.msc.GlobalConstants.CAMERA_PHOTO_PATH;

import static com.danthecodinggui.recipes.msc.GlobalConstants.PHOTOS_DIRECTORY_PATH;
import static io.fotoapparat.selector.AspectRatioSelectorsKt.standardRatio;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.LensPositionSelectorsKt.front;
import static io.fotoapparat.selector.PreviewFpsRangeSelectorsKt.highestFps;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static io.fotoapparat.selector.SensorSensitivitySelectorsKt.highestSensorSensitivity;

/**
 * Shows camera preview, can take photos with front/rear cameras with flash on/off. </br>
 * When a photo is taken, show a preview before returning to AddRecipeActivity.
 */
public class CameraActivity extends AppCompatActivity {

    ActivityCameraBinding binding;

    private Fotoapparat fotoapparat;

    //Instance State Tags
    private static final String FILE_PATH = "FILE_PATH";
    private static final String IS_CAMERA_BACK = "IS_CAMERA_BACK";
    private static final String CAMERA_FLASH = "CAMERA_FLASH";

    private int initOrientation;
    private OrientationListener orientationListener;

    private String photoFilesDir;

    private String resultCachePath;

    private int CAM_FLASH_AUTO = 1;
    private int CAM_FLASH_ON = 2;
    private int CAM_FLASH_OFF = 3;

    private boolean takenPhoto = false;

    private boolean isCameraBack = true;
    private int cameraFlash = CAM_FLASH_OFF;

    private CameraConfiguration cameraConfiguration = CameraConfiguration
            .builder()
            .photoResolution(standardRatio(
                    highestResolution()
            ))
            .focusMode(firstAvailable(
                    continuousFocusPicture(),
                    autoFocus(),
                    fixed()
            ))
            .flash(firstAvailable(
                    autoRedEye(),
                    autoFlash(),
                    torch(),
                    off()
            ))
            .previewFpsRange(highestFps())
            .sensorSensitivity(highestSensorSensitivity())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        //Setup fotoapparat instance
        fotoapparat = Fotoapparat.with(this)
                .into(binding.cameraView)
                .previewScaleType(ScaleType.CenterCrop)
                .photoResolution(ResolutionSelectorsKt.highestResolution())
                .flash(off())
                .lensPosition(LensPositionSelectorsKt.back())
                .build();

        //Only show flash/front camera buttons if the device has that feature
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            binding.imbFlash.setVisibility(View.VISIBLE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
            binding.imbSwitchCamera.setVisibility(View.VISIBLE);

        CoordinatorLayout.LayoutParams params;

        //Lock Orientation
        int rotation = ((WindowManager)getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        //Need init orientation data to rotate icons to match orientation
        switch (rotation) {
            case Surface.ROTATION_0:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case ROTATION_90:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

                params = (CoordinatorLayout.LayoutParams) binding.imbConfirmImage.getLayoutParams();
                params.setMargins(Utility.dpToPx(this, 16),
                        Utility.dpToPx(this, 16),
                        Utility.dpToPx(this, 60),
                        Utility.dpToPx(this, 16));
                binding.imbConfirmImage.setLayoutParams(params);
                break;
            case ROTATION_180:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            case ROTATION_270:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

                params = (CoordinatorLayout.LayoutParams) binding.imbDiscardImage.getLayoutParams();
                params.setMargins(Utility.dpToPx(this, 50), Utility.dpToPx(this, 30), 0, 0);
                binding.imbDiscardImage.setLayoutParams(params);

                params = (CoordinatorLayout.LayoutParams) binding.imbConfirmImage.getLayoutParams();
                params.setMargins(Utility.dpToPx(this, 16),
                        Utility.dpToPx(this, 16),
                        Utility.dpToPx(this, 16),
                        Utility.dpToPx(this, 16));
                binding.imbConfirmImage.setLayoutParams(params);
                break;
        }

        //Lock orientation
        setRequestedOrientation(initOrientation);

        orientationListener = new OrientationListener(this);

        photoFilesDir = getFilesDir().getPath().concat("/CameraActivityPhotos/");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FILE_PATH, resultCachePath);
        outState.putBoolean(IS_CAMERA_BACK, isCameraBack);
        outState.putInt(CAMERA_FLASH, cameraFlash);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        resultCachePath = savedInstanceState.getString(FILE_PATH);
        isCameraBack = savedInstanceState.getBoolean(IS_CAMERA_BACK);
        cameraFlash = savedInstanceState.getInt(CAMERA_FLASH);

        if (resultCachePath != null)
            ShowConfirmationView(false, null);

        if (cameraFlash == CAM_FLASH_ON)
            binding.imbFlash.setImageResource(R.drawable.ic_flash_on);
        else if (cameraFlash == CAM_FLASH_OFF)
            binding.imbFlash.setImageResource(R.drawable.ic_flash_off);

        if (!isCameraBack)
            binding.imbSwitchCamera.setImageResource(R.drawable.ic_camera_back);
    }

    @Override
    protected void onStart() {
        super.onStart();
        orientationListener.enable();
        fotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientationListener.disable();
        fotoapparat.stop();
    }

    @Override
    public void onBackPressed() {
        if (resultCachePath != null)
            DiscardPhoto(null);
        else
            super.onBackPressed();
    }

    /**
     * Take the photo, save into a temporary file and show the confirmation view
     * @param view
     */
    public void CapturePhoto(View view) {

        //Don't allow multiple photos being taken in quick succession
        if (takenPhoto)
            return;
        takenPhoto = true;

        //Manually deal with flash as fotoapparat flash doesn't work properly
        if (cameraFlash == CAM_FLASH_ON)
            fotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(torch()).build());
        PhotoResult result = fotoapparat.takePicture();
        if (cameraFlash == CAM_FLASH_ON)
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    fotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(off()).build());
                }
            }, 400);


        Utility.CreateDir(photoFilesDir);

        String filename = UUID.randomUUID().toString() + ".jpg";

        File tempPhoto = new File(photoFilesDir, filename);

        resultCachePath = tempPhoto.getAbsolutePath();

        result.saveToFile(tempPhoto);

        result.toBitmap().whenDone(new WhenDoneListener<BitmapPhoto>() {
            @Override
            public void whenDone(BitmapPhoto bitmapPhoto) {
                ShowConfirmationView(true, bitmapPhoto);
            }
        });
    }

    /**
     * Shows a full screen view presenting the taken photo and providing options on whether to discard
     * or confirm photo
     * @param fromBitmap Flag identifying whether bitmap just generated or working from a file path
     * @param b The optional bitmap if it has just been generated
     */
    private void ShowConfirmationView(boolean fromBitmap, @Nullable BitmapPhoto b) {

        TransitionManager.beginDelayedTransition(binding.clyCameraRoot);
        binding.cdlyImageConfirm.setVisibility(View.VISIBLE);

        //Compensate for orientation change
        float rotateCompensation = CompensateForRotation(b.rotationDegrees);

        RequestOptions options = new RequestOptions()
                .transform(new RotateTransformation(-rotateCompensation));

        Glide.with(CameraActivity.this)
                .setDefaultRequestOptions(options)
                .load(fromBitmap ? b.bitmap : resultCachePath)
                .into(binding.imvImageConfirm);

        takenPhoto = false;
    }

    /**
     * Calculate a rotation transformation to apply to the produced image to comply with the current
     * orientation
     * @param rotation The rotation provided by fotoapparat
     * @return The compensated rotation
     */
    private float CompensateForRotation(int rotation) {
        if (initOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                initOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            if (orientationListener.rotation == ROTATION_90)
                rotation -= 90;
            else if (orientationListener.rotation == ROTATION_180)
                rotation += 180;
            else if (orientationListener.rotation == ROTATION_270)
                rotation += 90;
        }
        else if (initOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            if (orientationListener.rotation == ROTATION_0)
                rotation += 90;
            else if (orientationListener.rotation == ROTATION_180)
                rotation -= 90;
            else if (orientationListener.rotation == ROTATION_270)
                rotation += 180;
        }
        else if (initOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            if (orientationListener.rotation == ROTATION_0)
                rotation -= 90;
            else if (orientationListener.rotation == ROTATION_90)
                rotation += 180;
            else if (orientationListener.rotation == ROTATION_180)
                rotation += 90;
        }
        else {
            if (orientationListener.rotation == ROTATION_0)
                rotation += 90;
            else if (orientationListener.rotation == ROTATION_90)
                rotation += 180;
            else if (orientationListener.rotation == ROTATION_180)
                rotation -= 90;
        }

        return rotation;
    }

    /**
     * Toggle what flash mode fotoapparat is currently using
     * @param view
     */
    public void ToggleFlash(View view) {

        //Animate old icon out
        PropertyValuesHolder translate = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 100);
        PropertyValuesHolder fadeOut = PropertyValuesHolder.ofFloat(View.ALPHA, 0);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbFlash, translate, fadeOut);
        anim.setDuration(50);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //Actually change flash setting for fotoapparat

//                if (cameraFlash == CAM_FLASH_AUTO) {
//                    binding.imbFlash.setImageResource(R.drawable.ic_flash_on);
//                    fotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(off()).build());
//                }
                if (cameraFlash == CAM_FLASH_ON)
                    binding.imbFlash.setImageResource(R.drawable.ic_flash_off);
                else if (cameraFlash == CAM_FLASH_OFF) {
                    binding.imbFlash.setImageResource(R.drawable.ic_flash_on);
                    //fotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(autoFlash()).build());
                }

                ++cameraFlash;
                if (cameraFlash == 4)
                    cameraFlash = CAM_FLASH_ON;

                //Animate new icon in
                PropertyValuesHolder translate = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -100, 0);
                PropertyValuesHolder fadein = PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1);
                ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbFlash, translate, fadein);
                anim.setDuration(50);
                anim.start();
            }
        });
        anim.start();
    }

    /**
     * Toggle which camera (front/rear) the preview should show
     * @param view
     */
    public void ToggleCameraDir(View view) {

        fotoapparat.switchTo(isCameraBack ? front() : back(), cameraConfiguration);

        //Animate the icon change
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbSwitchCamera, scaleX, scaleY);
        anim.setDuration(100);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //Change icon half way through
                binding.imbSwitchCamera.setImageResource(
                        isCameraBack ? R.drawable.ic_camera_front : R.drawable.ic_camera_back);

                //Animate second half of animation
                PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.f);
                PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.f);
                ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbSwitchCamera, scaleX, scaleY);
                anim.setDuration(100);
                anim.start();
            }
        });
        anim.start();
        isCameraBack = !isCameraBack;
    }

    /**
     * Confirm photo and return to AddRecipeActivity
     * @param view
     */
    public void ConfirmPhoto(View view) {

        Intent finishCamera = new Intent();
        finishCamera.putExtra(CAMERA_PHOTO_PATH, resultCachePath);
        finishCamera.putExtra(PHOTOS_DIRECTORY_PATH, photoFilesDir);

        if (resultCachePath != null)
            setResult(RESULT_OK, finishCamera);
        else
            setResult(RESULT_CANCELED, finishCamera);

        finish();
    }

    /**
     * Return to camera view and discard saved photo
     * @param view
     */
    public void DiscardPhoto(View view) {

        TransitionManager.beginDelayedTransition(binding.clyCameraRoot);
        binding.cdlyImageConfirm.setVisibility(View.GONE);

        //Delete photo(s)
        Utility.ClearDir(photoFilesDir);

        resultCachePath = null;
    }

    /**
     * Enables activity to rotate certain elements on initOrientation change rather than recreate whole layout
     */
    private class OrientationListener extends OrientationEventListener {

        private int rotation = 0;
        OrientationListener(Context context) { super(context); }

        @Override
        public void onOrientationChanged(int orientation) {

            float rotate = 0.f;
            boolean shouldAnimate = false;

            if( (orientation < 35 || orientation > 325) && rotation != ROTATION_0){ // PORTRAIT
                rotation = ROTATION_0;
                shouldAnimate = true;
            }
            else if( orientation > 145 && orientation < 215 && rotation != ROTATION_180){ // REVERSE PORTRAIT
                rotation = ROTATION_180;
                rotate = 180.f;
                shouldAnimate = true;
            }
            else if(orientation > 55 && orientation < 125 && rotation != ROTATION_270){ // REVERSE LANDSCAPE
                rotation = ROTATION_270;
                rotate = 270.f;
                shouldAnimate = true;
            }
            else if(orientation > 235 && orientation < 305 && rotation != ROTATION_90){ //LANDSCAPE
                rotation = ROTATION_90;
                rotate = 90.f;
                shouldAnimate = true;
            }

            //Adjust rotations based on activity entry orientation
            if (initOrientation ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                rotate -= 90.f;
            else if (initOrientation ==  ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                rotate += 90.f;

            //Deal with under/overflow
            if (rotate < 0)
                rotate = 270.f;
            else if (rotate > 270.f)
                rotate = 0;

            if (shouldAnimate) {
                ObjectAnimator animSwitch = ObjectAnimator.ofFloat(binding.imbSwitchCamera, View.ROTATION, rotate).setDuration(200);
                ObjectAnimator animFlash = ObjectAnimator.ofFloat(binding.imbFlash, View.ROTATION, rotate).setDuration(200);
                animSwitch.start();
                animFlash.start();

                //Views in confirm view (must update regardless if visible or not)
                ObjectAnimator animDiscard = ObjectAnimator.ofFloat(binding.imbDiscardImage, View.ROTATION, rotate).setDuration(200);
                ObjectAnimator animConfirm = ObjectAnimator.ofFloat(binding.imbConfirmImage, View.ROTATION, rotate).setDuration(200);
                animDiscard.start();
                animConfirm.start();
            }
        }
    }

    /**
     * Rotation Transformation for Bitmap
     */
    private class RotateTransformation extends BitmapTransformation {

        private float rotateRotationAngle;

        RotateTransformation(float rotateRotationAngle) {
            super();

            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();

            matrix.postRotate(rotateRotationAngle);

            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(("rotate" + rotateRotationAngle).getBytes());
        }
    }
}
