package com.danthecodinggui.recipes.msc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.danthecodinggui.recipes.R;

public class AnimUtils {

    public static void animateSearchToolbar(final Activity activity, final Toolbar toolbar, int numberOfMenuIcon, boolean containsOverflow, boolean shouldShow) {

        toolbar.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.white));

        Resources res = activity.getResources();

        if (shouldShow) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        Utility.isRightToLeft(res) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(250);
                createCircularReveal.start();
            }
            else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-toolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(220);
                toolbar.clearAnimation();
                toolbar.startAnimation(translateAnimation);
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((res.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        Utility.isRightToLeft(res) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(250);
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
                animationSet.setDuration(220);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        toolbar.setBackgroundColor(Utility.getThemeColor(activity, R.attr.colorPrimary));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                toolbar.startAnimation(animationSet);
            }
        }
    }

    public static void revealAddActivity(Activity activity, View viewRoot, int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(viewRoot.getWidth(), viewRoot.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
            circularReveal.setDuration(300);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            viewRoot.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            activity.finish();
        }
    }

    public static boolean unRevealAddActivity(final Activity activity, final View viewRoot, int revealX, int revealY, boolean openMenuOpen) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            activity.finish();
        } else {
            float finalRadius = (float) (Math.max(viewRoot.getWidth(), viewRoot.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    viewRoot, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(300);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewRoot.setVisibility(View.INVISIBLE);
                    activity.finish();
                }
            });

            circularReveal.start();

            //Close menu if open
            if (openMenuOpen)
                return true;
        }
        return false;
    }
}
