package com.danthecodinggui.recipes.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
