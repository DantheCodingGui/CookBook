package com.danthecodinggui.recipes.view.view_recipe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.danthecodinggui.recipes.R;

public class ViewRecipeActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private RelativeLayout previewContainer;
    private Toolbar toolbar;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO need to conditionally set this between recipe with/without photo, ie normal toolbar for no photo
        setContentView(R.layout.activity_view_recipe);

        toolbar = findViewById(R.id.tbar_vw_recipe);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.ctl_vw_recipe);
        collapsingToolbar.setTitle(getIntent().getStringExtra("Title"));

        final AppBarLayout appBarLayout = findViewById(R.id.abl_view_recipe);
        appBarLayout.addOnOffsetChangedListener(this);

        previewContainer = findViewById(R.id.rly_preview_container);
        tabLayout = findViewById(R.id.tly_view_recipe);

        //Generates one colour from the recipe preview photo to use as toolbar colour
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.sample_image);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                appBarLayout.setBackgroundColor(mutedColor);
            }
        });

        TabLayout tabLayout = findViewById(R.id.tly_view_recipe);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_ingredients)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_method)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        /*
        For adding icons to tabs later
        TODO Once you're at this stage, look into animated icons like in telegramX
        tabLayout.getTabAt(0).setIcon();
        tabLayout.getTabAt(1).setIcon();
        */

        final ViewPager viewPager = findViewById(R.id.vpr_view_recipe);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
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
        finish();
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int toolBarHeight = toolbar.getMeasuredHeight();
        int appBarHeight = appBarLayout.getMeasuredHeight();
        float transitionSpace = (float) appBarHeight - toolBarHeight - tabLayout.getMeasuredHeight();
        Float f = ((transitionSpace + verticalOffset) / transitionSpace) * 255;
        Log.d("graphics", "f: " + f.toString());
        previewContainer.getBackground().setAlpha(Math.round(f));
    }
}
