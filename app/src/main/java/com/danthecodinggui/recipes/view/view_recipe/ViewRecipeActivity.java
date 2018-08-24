package com.danthecodinggui.recipes.view.view_recipe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.danthecodinggui.recipes.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.danthecodinggui.recipes.msc.IntentConstants.CARD_TRANSITION_NAME;

public class ViewRecipeActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.tbar_vw_recipe) Toolbar toolbar;
    @BindView(R.id.tly_view_recipe) TabLayout tabLayout;
    @BindView(R.id.ivw_toolbar_preview) ImageView preview;

    boolean hasPhoto = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        if (hasPhoto) {
            setContentView(R.layout.activity_view_recipe_photo);
            //supportPostponeEnterTransition();

            //TODO change later to set based on database query
            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.ctl_vw_recipe);
            collapsingToolbar.setTitle(getIntent().getStringExtra("Title"));

            AppBarLayout appBarLayout = findViewById(R.id.abl_view_recipe);
            appBarLayout.addOnOffsetChangedListener(this);

            SetScrimColour(appBarLayout);

            //Set status bar colour
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        else {
            setContentView(R.layout.activity_view_recipe);
            //supportPostponeEnterTransition();
        }

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    String imageTransitionName = getIntent().getStringExtra(CARD_TRANSITION_NAME);
        //    findViewById(R.id.view_recipe_root).setTransitionName(imageTransitionName);
        //}

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SetupTabLayout();
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
    private void SetupTabLayout() {
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_ingredients);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_method);


        List<String> tabTitles = new ArrayList<>(Arrays.asList(
                getString(R.string.tab_ingredients),
                getString(R.string.tab_method)));

        final ViewPager viewPager = findViewById(R.id.vpr_view_recipe);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //Will set the alpha of the image based on collapsing toolbar scroll
        int toolBarHeight = toolbar.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        float transitionSpace = (float)appBarHeight - toolBarHeight;// - tabLayout.getMeasuredHeight() - 80;
        Float f = ((transitionSpace + verticalOffset) / transitionSpace) * 255;
        Log.d("graphics", "f: " + f.toString());
        preview.setImageAlpha(Math.round(f));
    }
}
