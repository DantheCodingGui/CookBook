package com.danthecodinggui.recipes.msc.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

import com.danthecodinggui.recipes.R;

/**
 * Set of utility methods related to animating views
 */
public class AnimUtils {

    /**
     * Fades the status bar colour from its current value to a new one
     * @param activity The source activity
     * @param newColour The new colour to animate to
     * @param interpolator The animation interpolator to use
     */
    public static void animateStatusBarColour(Activity activity, int newColour, Interpolator interpolator) {

        final Window window = activity.getWindow();

        ValueAnimator fadeStatusBar = ValueAnimator.ofObject(new ArgbEvaluator(),
                window.getStatusBarColor(),
                newColour);
        if (interpolator != null)
            fadeStatusBar.setInterpolator(interpolator);
        fadeStatusBar.addUpdateListener((valueAnimator) ->
                window.setStatusBarColor((int)valueAnimator.getAnimatedValue()));
        fadeStatusBar.start();
    }

    /**
     * Fades the status bar colour from its current value to a new one
     * @param activity The source activity
     * @param newColour The new colour to animate to
     */
    public static void animateStatusBarColour(Activity activity, int newColour) {
        animateStatusBarColour(activity, newColour, null);
    }

    /**
     * Animate an Animated Vector Drawable
     */
    public static void animateVectorDrawable(Drawable drawable) {
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        }
        else if (drawable instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
            avd.start();
        }
    }

    /**
     * Animate an Animated Vector Drawable instantly (for use when shouldn't run animation)
     */
    public static void instaAnimateVectorDrawable(Drawable drawable) {
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
            avd.stop();
        }
        else if (drawable instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
            avd.start();
            avd.stop();
        }
    }

    /**
     * Run a circular reveal animation on a searchview
     * @param activity The source activity
     * @param toolbar The toolbar the searchview resides in
     * @param numberOfMenuIcon The quantity of icons in the toolbar
     * @param containsOverflow Does the toolbar contain an overflow icon
     * @param shouldShow Should this animation reveal or collapse
     */
    public static void animateSearchToolbar(final Activity activity, final Toolbar toolbar, int numberOfMenuIcon, boolean containsOverflow, boolean shouldShow) {

        toolbar.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white));

        Resources res = activity.getResources();

        if (shouldShow) {
            if (Utility.atLeastLollipop()) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        Utility.isRightToLeft(res) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(200);
                createCircularReveal.start();
            }
            else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-toolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(200);
                toolbar.clearAnimation();
                toolbar.startAnimation(translateAnimation);
            }
        }
        else {
            if (Utility.atLeastLollipop()) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        Utility.isRightToLeft(res) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(200);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        toolbar.setBackgroundColor(Utility.getThemeColor(activity, R.attr.colorPrimary));
                    }
                });
                createCircularReveal.start();
            }
            else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-toolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(200);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        toolbar.setBackgroundColor(Utility.getThemeColor(activity, R.attr.colorPrimary));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                toolbar.startAnimation(animationSet);
            }
        }
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
}
