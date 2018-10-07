package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityCameraBinding;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.selector.LensPositionSelectorsKt;
import io.fotoapparat.selector.ResolutionSelectorsKt;

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

public class CameraActivity extends AppCompatActivity {

    ActivityCameraBinding binding;

    Fotoapparat fotoapparat;

    private int initOrientation;
    private OrientationListener orientationListener;

    public static Bitmap takenImage;

    private int CAM_FLASH_AUTO = 1;
    private int CAM_FLASH_ON = 2;
    private int CAM_FLASH_OFF = 3;

    private boolean isCameraBack = true;
    private int cameraFlash = CAM_FLASH_AUTO;

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

        fotoapparat = Fotoapparat.with(this)
                .into(binding.cameraView)
                .previewScaleType(ScaleType.CenterCrop)
                .photoResolution(ResolutionSelectorsKt.highestResolution())
                .lensPosition(LensPositionSelectorsKt.back())
                .build();

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            binding.imbFlash.setVisibility(View.VISIBLE);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
            binding.imbSwitchCamera.setVisibility(View.VISIBLE);

        //Lock Orientation
        int rotation = ((WindowManager)getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                initOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }

        setRequestedOrientation(initOrientation);

        orientationListener = new OrientationListener(this);
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

    public void CapturePhoto(View view) {
        PhotoResult result = fotoapparat.takePicture();
    }

    public void ToggleFlash(View view) {

        PropertyValuesHolder translate = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 100);
        PropertyValuesHolder fadeOut = PropertyValuesHolder.ofFloat(View.ALPHA, 0);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbFlash, translate, fadeOut);
        anim.setDuration(50);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cameraFlash == CAM_FLASH_AUTO) {
                    binding.imbFlash.setImageResource(R.drawable.ic_flash_on);
                    UpdateConfiguration.builder().flash(torch());
                }
                else if (cameraFlash == CAM_FLASH_ON) {
                    binding.imbFlash.setImageResource(R.drawable.ic_flash_off);
                    UpdateConfiguration.builder().flash(off());
                }
                else if (cameraFlash == CAM_FLASH_OFF) {
                    binding.imbFlash.setImageResource(R.drawable.ic_flash_auto);
                    UpdateConfiguration.builder().flash(autoFlash());
                }

                ++cameraFlash;
                if (cameraFlash == 4)
                    cameraFlash = CAM_FLASH_AUTO;

                PropertyValuesHolder translate = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -100, 0);
                PropertyValuesHolder fadein = PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1);
                ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbFlash, translate, fadein);
                anim.setDuration(50);
                anim.start();
            }
        });
        anim.start();
    }

    public void ToggleCameraDir(View view) {

        fotoapparat.switchTo(isCameraBack ? front() : back(), cameraConfiguration);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(binding.imbSwitchCamera, scaleX, scaleY);
        anim.setDuration(100);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.imbSwitchCamera.setImageResource(
                        isCameraBack ? R.drawable.ic_camera_front : R.drawable.ic_camera_back);

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
     * Enables activity to rotate certain elements on initOrientation change rather than recreate whole layout
     */
    private class OrientationListener extends OrientationEventListener {

        final int ROTATION_O    = 1;
        final int ROTATION_90   = 2;
        final int ROTATION_180  = 3;
        final int ROTATION_270  = 4;

        private int rotation = 0;
        OrientationListener(Context context) { super(context); }

        @Override
        public void onOrientationChanged(int orientation) {

            float rotate = 0.f;
            boolean shouldAnimate = false;

            if( (orientation < 35 || orientation > 325) && rotation!= ROTATION_O){ // PORTRAIT
                rotation = ROTATION_O;
                rotate = 0.f;
                shouldAnimate = true;
            }
            else if( orientation > 145 && orientation < 215 && rotation!=ROTATION_180){ // REVERSE PORTRAIT
                rotation = ROTATION_180;
                rotate = 180.f;
                shouldAnimate = true;
            }
            else if(orientation > 55 && orientation < 125 && rotation!=ROTATION_270){ // REVERSE LANDSCAPE
                rotation = ROTATION_270;
                rotate = 270.f;
                shouldAnimate = true;
            }
            else if(orientation > 235 && orientation < 305 && rotation!=ROTATION_90){ //LANDSCAPE
                rotation = ROTATION_90;
                rotate = 90.f;
                shouldAnimate = true;
            }

            //Adjust rotations based on activity entry orientation
            if (initOrientation ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                rotate -= 90.f;
            else if (initOrientation ==  ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
                rotate += 90.f;

            if (rotate < 0)
                rotate = 270.f;
            else if (rotate > 270.f)
                rotate = 0;

            if (shouldAnimate) {
                ObjectAnimator animSwitch = ObjectAnimator.ofFloat(binding.imbSwitchCamera, View.ROTATION, rotate).setDuration(200);
                ObjectAnimator animFlash = ObjectAnimator.ofFloat(binding.imbFlash, View.ROTATION, rotate).setDuration(200);
                animSwitch.start();
                animFlash.start();
            }
        }
    }
}
