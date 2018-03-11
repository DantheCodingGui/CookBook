package com.danthecodinggui.recipes.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.danthecodinggui.recipes.R;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

public class AddRecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        BuildFabMenu();
    }

    private void BuildFabMenu() {
        final ImageView menuIcon = new ImageView(getApplicationContext());
        menuIcon.setImageDrawable(getDrawable(R.drawable.ic_add));

        ImageView timeIcon = new ImageView(getApplicationContext());
        timeIcon.setImageDrawable(getDrawable(R.drawable.ic_time));

        ImageView photoIcon = new ImageView(getApplicationContext());
        photoIcon.setImageDrawable(getDrawable(R.drawable.ic_photo));

        ImageView kcalIcon = new ImageView(getApplicationContext());
        kcalIcon.setImageDrawable(getDrawable(R.drawable.ic_calories));

        FloatingActionButton menuButton = new FloatingActionButton.Builder(this)
                .setPosition(4)
                .setContentView(menuIcon)
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();

        SubActionButton time = new SubActionButton.Builder(this)
                .setContentView(timeIcon)
                .setLayoutParams(new FloatingActionButton.LayoutParams(175, 175))
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();
        SubActionButton photo = new SubActionButton.Builder(this)
                .setContentView(photoIcon)
                .setLayoutParams(new FloatingActionButton.LayoutParams(175, 175))
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();
        SubActionButton kcal = new SubActionButton.Builder(this)
                .setContentView(kcalIcon)
                .setLayoutParams(new FloatingActionButton.LayoutParams(175, 175))
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();

        FloatingActionMenu menu = new FloatingActionMenu.Builder(this)
                .addSubActionView(time)
                .addSubActionView(photo)
                .addSubActionView(kcal)
                .setStartAngle(170)
                .setEndAngle(285)
                .setRadius(250)
                .enableAnimations()
                .attachTo(menuButton)
                .build();

        menu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees clockwise
                menuIcon.setRotation(0);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 135);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(menuIcon, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees counter-clockwise
                menuIcon.setRotation(135);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(menuIcon, pvhR);
                animation.start();
            }
        });
    }
}
