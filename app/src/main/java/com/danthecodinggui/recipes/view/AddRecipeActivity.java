package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.danthecodinggui.recipes.R;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.EXTRA_CIRCULAR_REVEAL_X;
import static com.danthecodinggui.recipes.msc.IntentConstants.EXTRA_CIRCULAR_REVEAL_Y;

public class AddRecipeActivity extends AppCompatActivity {

    View root;

    private int revealX, revealY;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

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

        BuildFabMenu();
    }

    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(root.getWidth(), root.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(root, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            root.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unRevealActivity();
    }

    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(root.getWidth(), root.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    root, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(400);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    root.setVisibility(View.INVISIBLE);
                    finish();
                }
            });

            circularReveal.start();
        }
    }

    private void BuildFabMenu() {
        final ImageView menuIcon = new ImageView(getApplicationContext());
        menuIcon.setImageDrawable(getDrawable(R.drawable.ic_add));

        ImageView timeIcon = new ImageView(getApplicationContext());
        timeIcon.setImageDrawable(getDrawable(R.drawable.ic_time));

        ImageView photoIcon = new ImageView(getApplicationContext());
        photoIcon.setImageDrawable(getDrawable(R.drawable.ic_photo));

        ImageView kcalIcon = new ImageView(getApplicationContext());
        kcalIcon.setImageDrawable(getDrawable(R.drawable.ic_kcal));

        FloatingActionButton menuButton = new FloatingActionButton.Builder(this)
                .setPosition(4)
                .setContentView(menuIcon)
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();
        menuButton.setFocusable(false);

        SubActionButton time = new SubActionButton.Builder(this)
                .setContentView(timeIcon)
                .setLayoutParams(new FloatingActionButton.LayoutParams(175, 175))
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();
        time.setFocusable(false);
        SubActionButton photo = new SubActionButton.Builder(this)
                .setContentView(photoIcon)
                .setLayoutParams(new FloatingActionButton.LayoutParams(175, 175))
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();
        photo.setFocusable(false);
        SubActionButton kcal = new SubActionButton.Builder(this)
                .setContentView(kcalIcon)
                .setLayoutParams(new FloatingActionButton.LayoutParams(175, 175))
                .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
                .build();
        kcal.setFocusable(false);

        FloatingActionMenu menu = new FloatingActionMenu.Builder(this)
                .addSubActionView(time)
                .addSubActionView(photo)
                .addSubActionView(kcal)
                .setStartAngle(170)
                .setEndAngle(285)
                .setRadius(250)
                .enableAnimations()
                .attachTo(menuButton)
                .build();

        menu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees clockwise
                menuIcon.setRotation(0);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 135);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(menuIcon, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees counter-clockwise
                menuIcon.setRotation(135);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(menuIcon, pvhR);
                animation.start();
            }
        });
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
