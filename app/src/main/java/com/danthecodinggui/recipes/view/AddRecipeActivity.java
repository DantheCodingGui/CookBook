package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.danthecodinggui.recipes.R;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.EXTRA_CIRCULAR_REVEAL_X;
import static com.danthecodinggui.recipes.msc.IntentConstants.EXTRA_CIRCULAR_REVEAL_Y;

public class AddRecipeActivity extends AppCompatActivity {

    private View root;
    private int revealX, revealY;

    private FloatingActionButton openMenu;
    private boolean openMenuOpen = false;

    private FloatingActionButton addPhoto;
    private FloatingActionButton addTime;
    private FloatingActionButton addKcal;

    private TextView addPhotoTag;
    private TextView addTimeTag;
    private TextView addKcalTag;

    private static final int LIST_INGREDIENTS = 0;
    private static final int LIST_METHOD = 1;

    private RecyclerView ingredientsView;
    private IngredientsViewAdapter ingredientsAdapter;
    private List<String> ingredientsList;
    private EditText addIngredient;

    private RecyclerView methodView;
    private MethodViewAdapter methodAdapter;
    private List<String> methodList;
    private EditText addStep;

    private ImageView optionalPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        openMenu = findViewById(R.id.fab_add_menu);
        addPhoto = findViewById(R.id.fab_add_photo);
        addTime = findViewById(R.id.fab_add_time);
        addKcal = findViewById(R.id.fab_add_kcal);

        addPhotoTag = findViewById(R.id.txt_add_photo);
        addTimeTag = findViewById(R.id.txt_add_time);
        addKcalTag = findViewById(R.id.txt_add_kcal);

        optionalPreview = findViewById(R.id.imv_add_preview);

        //TODO Dude, you need to change these from textviews to edit texts, people will want to edit these at some point

        ingredientsView = findViewById(R.id.rvw_add_ingredients);
        ingredientsAdapter = new IngredientsViewAdapter();
        ingredientsList = new ArrayList<>();
        ingredientsView.setAdapter(ingredientsAdapter);
        ingredientsView.setLayoutManager(new NoScrollLinearLayout(getApplicationContext()));

        methodView = findViewById(R.id.rvw_add_method);
        methodAdapter = new MethodViewAdapter();
        methodList = new ArrayList<>();
        methodView.setAdapter(methodAdapter);
        methodView.setLayoutManager(new NoScrollLinearLayout(getApplicationContext()));

        addIngredient = findViewById(R.id.etxt_add_ingredient);
        addIngredient.setOnKeyListener(newIngredientListener);

        addStep = findViewById(R.id.etxt_add_step);
        addStep.setOnKeyListener(newStepListener);

        final Intent intent = getIntent();
        root = findViewById(R.id.add_root);

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            root.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = root.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
        else {
            root.setVisibility(View.VISIBLE);
        }

        Toolbar toolbar = findViewById(R.id.tbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SetupImageView();
    }

    private void SetupImageView() {

    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(root.getWidth(), root.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(root, x, y, 0, finalRadius);
            circularReveal.setDuration(300);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            root.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
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

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(root.getWidth(), root.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    root, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(300);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    root.setVisibility(View.INVISIBLE);
                    finish();
                }
            });

            circularReveal.start();

            //Close menu if open
            if (openMenuOpen)
                AnimateFabMenu(null);
        }
    }

    private void addItem(int listFlag) {

        EditText input;
        List<String> list;
        RecyclerView.Adapter adapter;

        if (listFlag == LIST_INGREDIENTS) {
            input = addIngredient;
            list = ingredientsList;
            adapter = ingredientsAdapter;
        }
        else {
            input = addStep;
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
            openMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_backwards));
            //TODO change this to list that you iterate through
            AnimateFabItem(addPhoto);
            AnimateFabItem(addPhotoTag);
            AnimateFabItem(addTime);
            AnimateFabItem(addTimeTag);
            AnimateFabItem(addKcal);
            AnimateFabItem(addKcalTag);
            openMenuOpen = false;
        }
        else {
            openMenu.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_forwards));
            AnimateFabItem(addPhoto);
            AnimateFabItem(addPhotoTag);
            AnimateFabItem(addTime);
            AnimateFabItem(addTimeTag);
            AnimateFabItem(addKcal);
            AnimateFabItem(addKcalTag);
            openMenuOpen = true;
        }
    }

    private void AnimateFabItem(View menuItem) {
        AnimationSet set = new AnimationSet(true);
        Animation rotate;

        float fabMenuXDelta = (openMenu.getX() + openMenu.getWidth() / 2) - (menuItem.getX() + menuItem.getWidth() / 2);
        float fabMenuYDelta = (openMenu.getY() + openMenu.getHeight() / 2) - (menuItem.getY() + menuItem.getHeight() / 2);

        if (openMenuOpen) {
            rotate = new RotateAnimation(0.f, -150.f, fabMenuXDelta + openMenu.getWidth() / 2, fabMenuYDelta + openMenu.getHeight() / 2);

            set.addAnimation(rotate);

            menuItem.setClickable(false);
        }
        else {
            Animation rotateBounce;

            rotate = new RotateAnimation(-150.f, 10.f, fabMenuXDelta + openMenu.getWidth() / 2, fabMenuYDelta + openMenu.getHeight() / 2);
            rotateBounce = new RotateAnimation(0.f, -10.f, fabMenuXDelta + openMenu.getWidth() / 2, fabMenuYDelta + openMenu.getHeight() / 2);
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
