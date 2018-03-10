package com.danthecodinggui.recipes.view;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.danthecodinggui.recipes.R;

public class AddRecipeActivity extends AppCompatActivity {

    private FloatingActionButton openMenu;
    private boolean openMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        openMenu = findViewById(R.id.fab_add_menu);
    }

    public void AnimateFab(View view) {
        if (openMenuOpen) {
            openMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_backwards));
            openMenuOpen = false;
        }
        else {
            openMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_forward));
            openMenuOpen = true;
        }
    }
}
