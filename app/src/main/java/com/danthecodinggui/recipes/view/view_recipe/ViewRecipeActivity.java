package com.danthecodinggui.recipes.view.view_recipe;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipeBinding;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipePhotoBinding;
import com.danthecodinggui.recipes.model.RecipeViewModel;
import com.danthecodinggui.recipes.msc.AnimUtils;
import com.danthecodinggui.recipes.msc.IntentConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.danthecodinggui.recipes.msc.IntentConstants.CARD_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DB_ID;

public class ViewRecipeActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    ActivityViewRecipeBinding binding;
    ActivityViewRecipePhotoBinding bindingPhoto;

    private String[] foodPhotos = {
            "https://images.pexels.com/photos/46239/salmon-dish-food-meal-46239.jpeg?cs=srgb&dl=food-salad-healthy-46239.jpg&fm=jpg",
            "https://images.pexels.com/photos/70497/pexels-photo-70497.jpeg?cs=srgb&dl=food-dinner-lunch-70497.jpg&fm=jpg",
            "https://images.pexels.com/photos/247685/pexels-photo-247685.png?cs=srgb&dl=food-plate-healthy-247685.jpg&fm=jpg",
            "https://images.pexels.com/photos/8313/food-eating-potatoes-beer-8313.jpg?auto=compress&cs=tinysrgb&h=350",
            "https://drop.ndtv.com/albums/COOKS/corngallery/creolespicedcornthumb_640x480.jpg",
            "https://cdn.cnn.com/cnnnext/dam/assets/171027052520-processed-foods-exlarge-tease.jpg",
            "https://drop.ndtv.com/albums/COOKS/pasta-vegetarian/pastaveg_640x480.jpg",
            "http://www.parkdeanholidays.co.uk/resources/images/foodanddrink/foodMainImg.jpg",
            "https://media.istockphoto.com/photos/health-food-for-fitness-picture-id855098134?k=6&m=855098134&s=612x612&w=0&h=eIWWpYWKTz_z2ryYAo0Dd97igUZVExzl4AKRIhUrFj4="
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        int recipeId = extras.getInt(RECIPE_DB_ID);
        boolean hasPhoto = extras.getBoolean("hasPhoto");

        RecipeViewModel recipe = new RecipeViewModel("Fish and Chips");

        //Todo change later to recipe.hasPhoto()
        if (hasPhoto) {

            bindingPhoto = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe_photo);
            supportPostponeEnterTransition();

            //TODO change later to set based on database query

            bindingPhoto.setRecipe(recipe);

            if (AnimUtils.canUseSharedTransitions()) {
                //Set the shared elements transition name
                String imageTransitionName = extras.getString(CARD_TRANSITION_NAME);
                bindingPhoto.ivwToolbarPreview.setTransitionName(imageTransitionName);

                supportStartPostponedEnterTransition();
            }

            bindingPhoto.ivwToolbarPreview.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.sample_image));

            String url = foodPhotos[new Random().nextInt(foodPhotos.length)];

            //TODO remove all url references when actually loading images (also remove internet privelige)
            Glide.with(this)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            SetScrimColour(bindingPhoto.ablViewRecipe, resource);
                            return false;
                        }
                    })
                    .into(bindingPhoto.ivwToolbarPreview);

            bindingPhoto.ablViewRecipe.addOnOffsetChangedListener(this);

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
    private void SetScrimColour(final AppBarLayout appBar, Drawable res) {
        BitmapDrawable drawable = (BitmapDrawable) res;
        Bitmap bitmap = drawable.getBitmap();

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
        //ie. the image is fully visible when scrolled down, but fades away into the toolbar colour
        // when scrolled up
        int toolBarHeight = bindingPhoto.tbarVwRecipe.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        float transitionSpace = (float)appBarHeight - toolBarHeight;// - tabLayout.getMeasuredHeight() - 80;
        Float f = ((transitionSpace + verticalOffset) / transitionSpace) * 255;
        bindingPhoto.ivwToolbarPreview.setImageAlpha(Math.round(f));
    }
}
