package com.danthecodinggui.recipes.view.view_recipe;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.danthecodinggui.recipes.BR;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipeBinding;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipePhotoBinding;
import com.danthecodinggui.recipes.model.RecipeViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.CARD_TRANSITION_NAME;

public class ViewRecipeActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    ActivityViewRecipeBinding binding;
    ActivityViewRecipePhotoBinding bindingPhoto;

    boolean hasPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasPhoto) {

            bindingPhoto = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe_photo);
            //supportPostponeEnterTransition();

            //TODO change later to set based on database query
            //bindingPhoto.ctlVwRecipe.setTitle(getIntent().getStringExtra("Title"));

            bindingPhoto.setRecipe(new RecipeViewModel("Fish and Chips"));

            //todo maybe pass this in w/ data binding to make easier
            bindingPhoto.ablViewRecipe.addOnOffsetChangedListener(this);

            SetScrimColour(bindingPhoto.ablViewRecipe);

            //Set status bar colour
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            setSupportActionBar(bindingPhoto.tbarVwRecipe);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            SetupTabLayout(bindingPhoto.tlyViewRecipe, bindingPhoto.vprViewRecipe);
        }
        else {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe);
            //supportPostponeEnterTransition();

            binding.setRecipe(new RecipeViewModel("Fish and Chips"));

            setSupportActionBar(binding.tbarVwRecipe);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            SetupTabLayout(binding.tlyViewRecipe, binding.vprViewRecipe);
        }

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    String imageTransitionName = getIntent().getStringExtra(CARD_TRANSITION_NAME);
        //    binding.viewRecipeRoot.setTransitionName(imageTransitionName);
        //}
    }

    /**
     * Generates one colour from the recipe preview photo to use as toolbar colour
     */
    private void SetScrimColour(final AppBarLayout appBar) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.sample_image);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                appBar.setBackgroundColor(mutedColor);
            }
        });
    }

    /**
     * Initialise ingredient/method tabs
     */
    private void SetupTabLayout(TabLayout tabLayout, final ViewPager viewPager) {
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Todo check why icons not setting

        List<String> tabTitles = new ArrayList<>(Arrays.asList(
                getString(R.string.tab_ingredients),
                getString(R.string.tab_method)));

        tabLayout.setupWithViewPager(viewPager);

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), tabTitles);
        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_ingredients);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_method);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //Will set the alpha of the image based on collapsing toolbar scroll
        int toolBarHeight = bindingPhoto.tbarVwRecipe.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        float transitionSpace = (float)appBarHeight - toolBarHeight;// - tabLayout.getMeasuredHeight() - 80;
        Float f = ((transitionSpace + verticalOffset) / transitionSpace) * 255;
        Log.d("graphics", "f: " + f.toString());
        bindingPhoto.ivwToolbarPreview.setImageAlpha(Math.round(f));
    }
}
