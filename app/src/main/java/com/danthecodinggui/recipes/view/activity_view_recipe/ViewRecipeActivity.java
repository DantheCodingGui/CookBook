package com.danthecodinggui.recipes.view.activity_view_recipe;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.BR;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipeBinding;
import com.danthecodinggui.recipes.databinding.ActivityViewRecipePhotoBinding;
import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.utility.AnimUtils;
import com.danthecodinggui.recipes.msc.MaterialColours;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.utility.Utility;
import com.danthecodinggui.recipes.view.activity_add_recipe.AddEditRecipeActivity;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_BUNDLE;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_STEPS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.IMAGE_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_BUNDLE;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_OBJECT;

/**
 * Display details of a specific recipe
 */
public class ViewRecipeActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener,
        IngredientsTabFragment.onIngredientsLoadedListener,
        MethodTabFragment.onMethodStepsLoadedListener {

    ActivityViewRecipeBinding binding;
    ActivityViewRecipePhotoBinding bindingPhoto;

    //TODO duplicate static value, find way to push into 1 class
    //Permission request codes
    private static final int REQ_CODE_READ_EXTERNAL = 211;

    //Instance State tags
    private static final String STATE_MATERIAL_COLOUR = "STATE_MATERIAL_COLOUR";
    private static final String ADDED_PHOTO = "ADDED_PHOTO";

    private String imageTransitionName;

    private int randIngredientsCol = -1;

    private Recipe recipe;
    private List<Ingredient> recipeIngredients;
    private List<MethodStep> recipeSteps;

    private boolean closingAnimating = false;
    private boolean addedPhoto = false;

    private ContentObserver contentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        recipe = extras.getBundle(RECIPE_DETAIL_BUNDLE).getParcelable(RECIPE_DETAIL_OBJECT);

        if (recipe.hasPhoto()) {
            imageTransitionName = extras.getString(IMAGE_TRANSITION_NAME);

            AskPhotoLayoutPerm();
        }
        else {
            if (savedInstanceState != null) {
                randIngredientsCol = savedInstanceState.getInt(STATE_MATERIAL_COLOUR);
                addedPhoto = savedInstanceState.getBoolean(ADDED_PHOTO);
            }
            SetupNoPhotoLayout();
        }

        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {

                new UpdateViewedRecipeTask(ViewRecipeActivity.this,
                        (updatedRecipe) -> {

                        //Needed to ensure that added photo doesn't mess with activity transitions
                        if (!recipe.hasPhoto() && updatedRecipe.hasPhoto())
                            addedPhoto = true;

                        recipe = updatedRecipe;
                        binding = null;
                        bindingPhoto = null;

                        //Pretty much just load the whole ui again
                        // (should make new ingredient/method fragments and update their values)
                        if (updatedRecipe.hasPhoto())
                            AskPhotoLayoutPerm();
                        else
                            SetupNoPhotoLayout();

                        //Must call again as new binding will overwrite view
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                ).execute(recipe.getRecipeId());
            }
        };
        getContentResolver().registerContentObserver(ProviderContract.RECIPES_URI, false, contentObserver);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void AskPhotoLayoutPerm() {
        int response = PermissionsHandler.AskForPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL);

        switch (response) {
            case PermissionsHandler.PERMISSION_GRANTED:
                SetupPhotoLayout();
                break;
            case PermissionsHandler.PERMISSION_DENIED:
                SetupNoPhotoLayout();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(STATE_MATERIAL_COLOUR, randIngredientsCol);
        savedInstanceState.putBoolean(ADDED_PHOTO, addedPhoto);
    }

    @Override
    public void onIngredientsLoaded(List<Ingredient> ingredients) {
        recipeIngredients = new ArrayList<>(ingredients);
    }

    @Override
    public void onMethodStepsLoaded(List<MethodStep> steps) {
        recipeSteps = new ArrayList<>(steps);
    }

    /**
     * Setup layout with CollapsingToolbarLayout and all it's components, including the relevant
     *  data bindings
     */
    private void SetupPhotoLayout() {

        getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_photo_enter));
        getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_photo_return));

        bindingPhoto = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe_photo);
        bindingPhoto.setRecipe(recipe);
        bindingPhoto.setVariable(BR.imageLoadedCallback, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                SetScrimColour(bindingPhoto.ablViewRecipe, resource);
                startPostponedEnterTransition();
                return false;
            }
        });

        //Resize CollapsingToolbarLayout title text size based on title length
        int titleSize = recipe.getTitle().length();
        //Need to cast as CollapsingToolbarLayout is a library implementation
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)bindingPhoto.ctlVwRecipe;
        if (titleSize <= 5)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleMax);
        else if (titleSize <= 10)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleLarge);
        else if (titleSize <= 20)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleMedium);
        else if (titleSize <= 30)
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleSmall);
        else
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsingToolbarTitleMin);

        if (!recipe.hasExtendedInfo())
            collapsingToolbarLayout.setExpandedTitleMarginEnd(Utility.dpToPx(30));

        if (Utility.atLeastLollipop()) {
            //Set the shared elements transition name
            postponeEnterTransition();
            bindingPhoto.ivwToolbarPreview.setTransitionName(imageTransitionName);

            //Set status bar colour
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            window.getSharedElementReturnTransition().setStartDelay(200);
        }

        bindingPhoto.ablViewRecipe.addOnOffsetChangedListener(this);

        setSupportActionBar(bindingPhoto.tbarVwRecipe);

        SetupTabLayout(bindingPhoto.tlyViewRecipe, bindingPhoto.vprViewRecipe);
    }

    /**
     * Setup layout with standard toolbar and tablayout, including the relevant data bindings
     */
    private void SetupNoPhotoLayout() {

        getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_enter));
        getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.view_activity_return));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_recipe);

        //Resize Toolbar title text size based on title length
        int titleSize = recipe.getTitle().length();

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT || Utility.isMultiWindow(this)) {

            if (titleSize <= 20)
                binding.tbarVwRecipe.setTitleTextAppearance(this, R.style.ToolbarTitleMax);
            else if (titleSize <= 24)
                binding.tbarVwRecipe.setTitleTextAppearance(this, R.style.ToolbarTitleLarge);
            else if (titleSize <= 28)
                binding.tbarVwRecipe.setTitleTextAppearance(this, R.style.ToolbarTitleMedium);
            else if (titleSize <= 32)
                binding.tbarVwRecipe.setTitleTextAppearance(this, R.style.ToolbarTitleSmall);
            else
                binding.tbarVwRecipe.setTitleTextAppearance(this, R.style.ToolbarTitleMin);
        }

        if (recipe.hasExtendedInfo())
            binding.clyVwOptionals.setVisibility(View.VISIBLE);

        binding.setRecipe(recipe);

        setSupportActionBar(binding.tbarVwRecipe);

        //Set random colour of layout
        SetLayoutColour();

        SetupTabLayout(binding.tlyViewRecipe, binding.vprViewRecipe);
    }

    /**
     * In the event of no stored photo for the recipe, generate a random material design colour and
     * apply it to the toolbar
     */
    private void SetLayoutColour() {
        if (randIngredientsCol == -1)
            randIngredientsCol = MaterialColours.nextColour();

        binding.tbarVwRecipe.setBackgroundColor(randIngredientsCol);
        binding.clyVwOptionals.setBackgroundColor(randIngredientsCol);
        binding.tlyViewRecipe.setBackgroundColor(randIngredientsCol);
        binding.vwFragBackground.setBackgroundColor(randIngredientsCol);

        if (Utility.atLeastLollipop()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(randIngredientsCol);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_activity_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_view_edit:

                Intent editRecipe = new Intent(getApplicationContext(), AddEditRecipeActivity.class);

                Bundle extras = new Bundle();
                extras.putParcelable(EDIT_RECIPE_OBJECT, recipe);
                extras.putParcelableArrayList(EDIT_RECIPE_INGREDIENTS, new ArrayList<>(recipeIngredients));
                extras.putParcelableArrayList(EDIT_RECIPE_STEPS, new ArrayList<>(recipeSteps));

                editRecipe.putExtra(EDIT_RECIPE_BUNDLE, extras);


                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
                startActivity(editRecipe, options.toBundle());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Generates one colour from the recipe preview photo to use as toolbar colour
     */
    private void SetScrimColour(final AppBarLayout appBar, Drawable res) {
        BitmapDrawable drawable = (BitmapDrawable) res;
        Bitmap bitmap = drawable.getBitmap();

        Palette.from(bitmap).generate((palette) -> {
                int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                appBar.setBackgroundColor(mutedColor);
            }
        );
    }

    /**
     * Initialise ingredient/method tabs
     */
    private void SetupTabLayout(TabLayout tabLayout, final ViewPager viewPager) {
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        List<String> tabTitles = new ArrayList<>(Arrays.asList(
                getString(R.string.ingredients),
                getString(R.string.method)));

        tabLayout.setupWithViewPager(viewPager);

        RecipePagerAdapter pagerAdapter = new RecipePagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount(), tabTitles, recipe.getRecipeId());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                Drawable icon = tab.getIcon();

                if (tab.getPosition() == 0)
                    AnimUtils.animateVectorDrawable(icon);
                else if (tab.getPosition() == 1)
                    AnimUtils.animateVectorDrawable(icon);
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
    public void onBackPressed() {
        if (bindingPhoto != null) {
            //Animate the appbar layout to expand before exiting
            bindingPhoto.ablViewRecipe.setExpanded(true, true);
            closingAnimating = true;
        }
        else if (!addedPhoto) {
            getContentResolver().unregisterContentObserver(contentObserver);
            supportFinishAfterTransition();
        }
        else {
            getContentResolver().unregisterContentObserver(contentObserver);
            finish();
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //Fades the scrim colour into the photo as appbar scrolls up/down
        //Fully extended = normal image, Fully retracted = image partially obscured by colour
        int toolBarHeight = bindingPhoto.tbarVwRecipe.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        float transitionSpace = (float)appBarHeight - toolBarHeight;

        Float f = ((transitionSpace + verticalOffset) / transitionSpace) * 255;
        bindingPhoto.ivwToolbarPreview.setImageAlpha(Math.round(f));

        //When AppBarLayout expansion fully animated, THEN the activity can close
        if (closingAnimating && verticalOffset == 0) {
            getContentResolver().unregisterContentObserver(contentObserver);
            if (addedPhoto)
                finish();
            else
                supportFinishAfterTransition();
        }

        float adjustedF = (f - 102) / 0.255f - 200;
        if (adjustedF < 0)
            adjustedF = 0;

        bindingPhoto.txtVwDuration.setTranslationX(400 - adjustedF);
        bindingPhoto.txtVwKcal.setTranslationX(400 - adjustedF);
    }
}
