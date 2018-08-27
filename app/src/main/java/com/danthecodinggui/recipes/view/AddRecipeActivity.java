package com.danthecodinggui.recipes.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityAddRecipeBinding;
import com.danthecodinggui.recipes.msc.AnimUtils;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.EXTRA_CIRCULAR_REVEAL_X;
import static com.danthecodinggui.recipes.msc.IntentConstants.EXTRA_CIRCULAR_REVEAL_Y;

public class AddRecipeActivity extends AppCompatActivity {

    ActivityAddRecipeBinding binding;

    private int revealX, revealY;

    private boolean openMenuOpen = false;

    private static final int LIST_INGREDIENTS = 0;
    private static final int LIST_METHOD = 1;

    private IngredientsViewAdapter ingredientsAdapter;
    private List<String> ingredientsList;

    private MethodViewAdapter methodAdapter;
    private List<String> methodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe);
        //TODO maybe make a layout without photo to support devices without a camera


        //TODO Dude, you need to change these from textviews to edit texts, people will want to edit these at some point

        ingredientsAdapter = new IngredientsViewAdapter();
        ingredientsList = new ArrayList<>();
        binding.rvwAddIngredients.setAdapter(ingredientsAdapter);
        binding.rvwAddIngredients.setLayoutManager(new NoScrollLinearLayout(getApplicationContext()));

        methodAdapter = new MethodViewAdapter();
        methodList = new ArrayList<>();
        binding.rvwAddMethod.setAdapter(methodAdapter);
        binding.rvwAddMethod.setLayoutManager(new NoScrollLinearLayout(getApplicationContext()));

        binding.incAddIngredient.etxtAddIngredient.setOnKeyListener(newIngredientListener);

        binding.incAddStep.etxtAddStep.setOnKeyListener(newStepListener);

        final Intent intent = getIntent();

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            binding.addRoot.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = binding.addRoot.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        AnimUtils.revealAddActivity(AddRecipeActivity.this, binding.addRoot, revealX, revealY);
                        binding.addRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
        else {
            binding.addRoot.setVisibility(View.VISIBLE);
        }

        setSupportActionBar(binding.tbarAdd);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SetupImageView();
    }

    private void SetupImageView() {

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                unRevealActivity();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        unRevealActivity();
    }

    private void unRevealActivity() {
        if (AnimUtils.unRevealAddActivity(this, binding.addRoot, revealX, revealY, openMenuOpen))
            AnimateFabMenu(null);
    }

    private void addItem(int listFlag) {

        EditText input;
        List<String> list;
        RecyclerView.Adapter adapter;

        if (listFlag == LIST_INGREDIENTS) {
            input = binding.incAddIngredient.etxtAddIngredient;
            list = ingredientsList;
            adapter = ingredientsAdapter;
        }
        else {
            input = binding.incAddStep.etxtAddStep;
            list = methodList;
            adapter = methodAdapter;
        }

        String ingredient = input.getText().toString();
        list.add(ingredient);
        adapter.notifyItemChanged(list.size());

        input.getText().clear();

        input.clearFocus();
        input.requestFocus();
    }

    TextView.OnKeyListener newIngredientListener = new TextView.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            //Detect when enter pressed while in edittext
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_ENTER) {
                addItem(LIST_INGREDIENTS);
                return true;
            }
            return false;
        }
    };

    TextView.OnKeyListener newStepListener = new TextView.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            //Detect when enter pressed while in edittext
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_ENTER) {
                addItem(LIST_METHOD);
                return true;
            }
            return false;
        }
    };

    public void AnimateFabMenu(View view) {
        if (openMenuOpen) {
            binding.fabAddMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_backwards));
            //TODO change this to list that you iterate through
            AnimateFabItem(binding.fabAddPhoto);
            AnimateFabItem(binding.txtAddPhoto);
            AnimateFabItem(binding.fabAddTime);
            AnimateFabItem(binding.txtAddTime);
            AnimateFabItem(binding.fabAddKcal);
            AnimateFabItem(binding.txtAddKcal);
            openMenuOpen = false;
        }
        else {
            binding.fabAddMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_forwards));
            AnimateFabItem(binding.fabAddPhoto);
            AnimateFabItem(binding.txtAddPhoto);
            AnimateFabItem(binding.fabAddTime);
            AnimateFabItem(binding.txtAddTime);
            AnimateFabItem(binding.fabAddKcal);
            AnimateFabItem(binding.txtAddKcal);
            openMenuOpen = true;
        }
    }

    private void AnimateFabItem(View menuItem) {
        AnimationSet set = new AnimationSet(true);
        Animation rotate;

        float fabMenuXDelta = (binding.fabAddMenu.getX() + binding.fabAddMenu.getWidth() / 2) - (menuItem.getX() + menuItem.getWidth() / 2);
        float fabMenuYDelta = (binding.fabAddMenu.getY() + binding.fabAddMenu.getHeight() / 2) - (menuItem.getY() + menuItem.getHeight() / 2);

        if (openMenuOpen) {
            rotate = new RotateAnimation(0.f, -150.f, fabMenuXDelta + binding.fabAddMenu.getWidth() / 2, fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);

            set.addAnimation(rotate);

            menuItem.setClickable(false);
        }
        else {
            Animation rotateBounce;

            rotate = new RotateAnimation(-150.f, 10.f, fabMenuXDelta + binding.fabAddMenu.getWidth() / 2, fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            rotateBounce = new RotateAnimation(0.f, -10.f, fabMenuXDelta + binding.fabAddMenu.getWidth() / 2, fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            rotateBounce.setStartOffset(250);
            rotateBounce.setDuration(500);

            set.addAnimation(rotate);
            set.addAnimation(rotateBounce);

            menuItem.setClickable(true);
        }

        set.setDuration(300);
        set.setFillAfter(true);

        menuItem.startAnimation(set);
    }

    class IngredientsViewAdapter extends RecyclerView.Adapter<IngredientsViewAdapter.IngredientViewHolder> {

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ingredient_item, parent, false));
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, int position) {
            holder.ingredient.setText(getString(R.string.txt_ingredient_item, ingredientsList.get(position)));
        }

        @Override
        public int getItemCount() {
            return ingredientsList.size();
        }

        class IngredientViewHolder extends RecyclerView.ViewHolder {

            TextView ingredient;

            IngredientViewHolder(View itemView) {
                super(itemView);
                ingredient = itemView.findViewById(R.id.txt_method_item);
            }
        }
    }

    class MethodViewAdapter extends RecyclerView.Adapter<MethodViewAdapter.StepViewHolder> {

        @Override
        public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StepViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.method_item, parent, false));
        }

        @Override
        public void onBindViewHolder(StepViewHolder holder, int position) {
            holder.step.setText(getString(R.string.txt_method_step_item, methodList.get(position)));
        }

        @Override
        public int getItemCount() {
            return methodList.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {

            TextView step;

            StepViewHolder(View itemView) {
                super(itemView);
                step = itemView.findViewById(R.id.txt_method_item);
            }
        }
    }
}
