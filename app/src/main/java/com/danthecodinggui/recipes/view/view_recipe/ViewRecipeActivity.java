package com.danthecodinggui.recipes.view.view_recipe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.danthecodinggui.recipes.R;

public class ViewRecipeActivity extends AppCompatActivity {

    private int mutedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO need to conditionally set this between recipe with/without photo, ie normal toolbar for no photo
        setContentView(R.layout.activity_view_recipe);

        Toolbar toolbar = findViewById(R.id.tbar_vw_recipe);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.ctl_vw_recipe);
        collapsingToolbar.setTitle(getIntent().getStringExtra("Title"));

        ImageView header = findViewById(R.id.ivw_toolbar_preview);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.sample_image);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                collapsingToolbar.setContentScrimColor(mutedColor);
            }
        });

        TabLayout tabLayout = findViewById(R.id.tly_view_recipe);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_ingredients)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_method)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

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
}
