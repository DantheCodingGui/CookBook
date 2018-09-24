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
}
