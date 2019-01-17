package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.transition.Slide;
import android.support.transition.TransitionInflater;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.transition.TransitionManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.asksira.bsimagepicker.BSImagePicker;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityAddRecipeBinding;
import com.danthecodinggui.recipes.databinding.AddIngredientItemBinding;
import com.danthecodinggui.recipes.databinding.AddMethodItemBinding;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.utility.FileUtils;
import com.danthecodinggui.recipes.msc.utility.StringUtils;
import com.danthecodinggui.recipes.msc.utility.Utility;
import com.danthecodinggui.recipes.view.CameraActivity;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperAdapter;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperViewHolder;
import com.danthecodinggui.recipes.view.ItemTouchHelper.OnStartDragListener;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.CAMERA_PHOTO_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_BUNDLE;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.EDIT_RECIPE_STEPS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.INGREDIENT_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.METHOD_STEP_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.PHOTOS_DIRECTORY_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_CAMERA_DIR_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_IS_IMAGE_CAMERA;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_METHOD;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_OLD_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_OLD_METHOD;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_RECIPE;

/**
 * Provides functionality to add_activity_toolbar recipes
 */
public class AddEditRecipeActivity extends AppCompatActivity implements
        CaloriesPickerFragment.onCaloriesSetListener,
        DurationPickerFragment.onDurationSetListener, AddImageURLFragment.onURLSetListener,
        BSImagePicker.OnSingleImageSelectedListener {

    ActivityAddRecipeBinding binding;

    private boolean isPortrait;

    private boolean isAdding = true;

    //Fragment Tags
    private static final String FRAG_TAG_TIME = "FRAG_TAG_TIME";
    private static final String FRAG_TAG_KCAL = "FRAG_TAG_KCAL";
    private static final String FRAG_TAG_EDIT_INGREDIENT = "FRAG_TAG_EDIT_INGREDIENT";
    private static final String FRAG_TAG_EDIT_STEP = "FRAG_TAG_EDIT_STEP";
    private static final String FRAG_TAG_IMAGE_URL = "FRAG_TAG_IMAGE_URL";
    private static final String FRAG_TAG_IMAGE_GALLERY = "FRAG_TAG_IMAGE_GALLERY";

    //Permission Request Codes
    private static final int PERM_REQ_CODE_CAMERA = 201;
    private static final int PERM_REQ_CODE_READ_EXTERNAL = 202;

    //Activity Request Codes
    private static final int ACT_REQ_CODE_CAMERA = 301;

    //Instance State Tags
    private static final String DURATION = "DURATION";
    private static final String KCAL = "KCAL";
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private static final String PHOTO_DIR_PATH = "PHOTO_DIR_PATH";
    private static final String IS_IMAGE_CAM = "IS_IMAGE_CAM";
    private static final String INGREDIENTS_EXPANDED = "INGREDIENTS_EXPANDED";
    private static final String METHOD_EXPANDED = "METHOD_EXPANDED";
    private static final String FAB_MENU_OPEN = "FAB_MENU_OPEN";
    private static final String PHOTO_SHEET_OPEN = "PHOTO_SHEET_OPEN";
    private static final String INGREDIENTS_LIST = "INGREDIENTS_LIST";
    private static final String METHOD_LIST = "METHOD_LIST";
    private static final String EDITING_POSITION = "EDITING_POSITION";

    //Various Flags
    private boolean fabMenuOpen = false;
    private boolean photoSheetExpanded = false;
    private boolean ingredientsExpanded = false;
    private boolean methodExpanded = false;
    private boolean isImageFromCam = false;
    private boolean isQuantityEmpty = true;
    private boolean isIngredientEmpty = true;

    //Recipe Data
    private int recipeDuration;
    private int recipeKcalPerPerson;
    private String currentImagePath;
    private String photosDirPath;
    private List<Ingredient> newIngredients;
    private List<MethodStep> newSteps;

    //Required when editing recipe to compare ingredients/steps
    private List<Ingredient> oldIngredients;
    private List<MethodStep> oldSteps;
    private long editRecipeId;

    private IngredientsAddAdapter ingAdapter;
    private MethodStepAddAdapter methAdapter;

    private int editingPosition = -1;

    private BottomSheetBehavior photoSheetBehaviour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            isPortrait = false;
        else {
            isPortrait = true;
            if (Utility.isMultiWindow(this)) {
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) binding.spcAdd.getLayoutParams();
                params.height = Utility.dpToPx(1);
            }
        }

        binding.rvwNewIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingAdapter = new IngredientsAddAdapter();
        binding.rvwNewIngredients.setAdapter(ingAdapter);
        ItemTouchHelper.Callback ingTouchCallback = new AddItemTouchHelperCallback(ingAdapter);
        final ItemTouchHelper ingTouchHelper = new ItemTouchHelper(ingTouchCallback);
        ingTouchHelper.attachToRecyclerView(binding.rvwNewIngredients);
        ingAdapter.setStartDragListener(ingTouchHelper::startDrag);

        binding.rvwNewSteps.setLayoutManager((new LinearLayoutManager(this)));
        methAdapter = new MethodStepAddAdapter();
        binding.rvwNewSteps.setAdapter(methAdapter);
        ItemTouchHelper.Callback methTouchCallback = new AddItemTouchHelperCallback(methAdapter);
        final ItemTouchHelper methTouchHelper = new ItemTouchHelper(methTouchCallback);
        methTouchHelper.attachToRecyclerView(binding.rvwNewSteps);
        methAdapter.setStartDragListener(methTouchHelper::startDrag);

        //Setup toolbar
        setSupportActionBar(binding.tbarAdd);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SetupLayoutAnimator();

        //Setup button enable/disable based on edittext contents
        binding.butAddIngredient.setEnabled(false);
        binding.etxtAddIngredientName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isIngredientEmpty = charSequence.length() == 0;

                if (isIngredientEmpty)
                    binding.butAddIngredient.setEnabled(false);
                else
                    binding.butAddIngredient.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        binding.butAddStep.setEnabled(false);
        binding.etxtAddStep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Utility.CheckButtonEnabled(binding.butAddStep, charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        //Populate spinner for ingredients input
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.default_ingredient_measurements, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spnIngredientMeasurement.setAdapter(spinnerAdapter);

        //Setup add image photo sheet
        photoSheetBehaviour = BottomSheetBehavior.from(binding.includeImageSheet.addImage);
        photoSheetBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    photoSheetExpanded = true;
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    photoSheetExpanded = false;
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        //Disable camera option if device doesn't have camera
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            binding.includeImageSheet.btnAddPhoto.setVisibility(View.GONE);

        PopulateEditRecipe(getIntent().getBundleExtra(EDIT_RECIPE_BUNDLE));
    }

    /**
     * Populate input views with existing data on recipe to edit
     * @param editBundle Bundle containing recipe to edit
     */
    private void PopulateEditRecipe(Bundle editBundle) {
        Recipe recipeToEdit;
        if (editBundle != null) {
            //We know we are editing a recipe
            isAdding = false;

            recipeToEdit = editBundle.getParcelable(EDIT_RECIPE_OBJECT);
            binding.etxtRecipeName.setText(recipeToEdit.getTitle());
            if (recipeToEdit.hasPhoto())
                SetImage(recipeToEdit.getImagePath());

            editRecipeId = recipeToEdit.getRecipeId();

            recipeDuration = recipeToEdit.getTimeInMins();
            recipeKcalPerPerson = recipeToEdit.getCalories();

            if (recipeDuration != 0)
                onDurationSet(recipeDuration);
            if (recipeKcalPerPerson != 0)
                onCaloriesSet(recipeKcalPerPerson);

            List<Ingredient> ingredients = editBundle.<Ingredient>getParcelableArrayList(EDIT_RECIPE_INGREDIENTS);
            List<MethodStep> steps = editBundle.<MethodStep>getParcelableArrayList(EDIT_RECIPE_STEPS);

            if (ingredients != null) {
                newIngredients = new ArrayList<>(ingredients);
                newSteps = new ArrayList<>(steps);

                //Must deep copy lists to copy items by value and not reference

                oldIngredients = new ArrayList<>();
                for (Ingredient in: ingredients)
                    oldIngredients.add(new Ingredient(in));

                oldSteps = new ArrayList<>();
                for (MethodStep step: steps)
                    oldSteps.add(new MethodStep(step));
            }

            //Must wait briefly until view dimensions can be accessed
            new Handler(getMainLooper()).postDelayed(() -> {
                        ShowRetractedIngredients();
                        ShowRetractedSteps();
                    }
                    , 10);
        }
        else {
            newIngredients = new ArrayList<>();
            newSteps = new ArrayList<>();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Recipe data
        outState.putInt(DURATION, recipeDuration);
        outState.putInt(KCAL, recipeKcalPerPerson);
        outState.putString(IMAGE_PATH, currentImagePath);
        outState.putString(PHOTO_DIR_PATH, photosDirPath);
        outState.putBoolean(IS_IMAGE_CAM, isImageFromCam);

        outState.putInt(EDITING_POSITION, editingPosition);

        //Flags
        outState.putBoolean(INGREDIENTS_EXPANDED, ingredientsExpanded);
        outState.putBoolean(METHOD_EXPANDED, methodExpanded);
        outState.putBoolean(FAB_MENU_OPEN, fabMenuOpen);
        outState.putBoolean(PHOTO_SHEET_OPEN, photoSheetExpanded);

        //Lists
        outState.putParcelableArrayList(INGREDIENTS_LIST, new ArrayList<>(newIngredients));
        outState.putParcelableArrayList(METHOD_LIST, new ArrayList<>(newSteps));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        recipeDuration = savedInstanceState.getInt(DURATION);
        recipeKcalPerPerson = savedInstanceState.getInt(KCAL);
        currentImagePath = savedInstanceState.getString(IMAGE_PATH);
        photosDirPath = savedInstanceState.getString(PHOTO_DIR_PATH);
        isImageFromCam = savedInstanceState.getBoolean(IS_IMAGE_CAM);
        ingredientsExpanded = savedInstanceState.getBoolean(INGREDIENTS_EXPANDED);
        methodExpanded = savedInstanceState.getBoolean(METHOD_EXPANDED);
        boolean fabMenuOpen = savedInstanceState.getBoolean(FAB_MENU_OPEN);
        photoSheetExpanded = savedInstanceState.getBoolean(PHOTO_SHEET_OPEN);

        //Restore ingredient and method steps
        newIngredients = savedInstanceState.getParcelableArrayList(INGREDIENTS_LIST);
        if (!newIngredients.isEmpty())
            ShowRetractedIngredients();

        newSteps = savedInstanceState.getParcelableArrayList(METHOD_LIST);
        if (!newSteps.isEmpty())
            ShowRetractedSteps();

        //Restore views
        if (recipeDuration != 0)
            onDurationSet(recipeDuration);
        if (recipeKcalPerPerson != 0)
            onCaloriesSet(recipeKcalPerPerson);
        if (currentImagePath != null)
            SetImage(currentImagePath);
        if (ingredientsExpanded)
            ExpandIngredientsCard();
        else if (methodExpanded)
            ExpandMethodCard();
        if (fabMenuOpen)
            AnimateFabMenu(binding.fabAddMenu);

        editingPosition = savedInstanceState.getInt(EDITING_POSITION);

        //Restore fragments
        DurationPickerFragment timeFrag = (DurationPickerFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_TIME);
        CaloriesPickerFragment kcalFrag = (CaloriesPickerFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_KCAL);
        EditIngredientFragment editIng = (EditIngredientFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_EDIT_INGREDIENT);
        EditMethodStepFragment editStep = (EditMethodStepFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_EDIT_STEP);
        AddImageURLFragment addURL = (AddImageURLFragment) getFragmentManager().findFragmentByTag(FRAG_TAG_IMAGE_URL);
        if (timeFrag != null)
            timeFrag.SetDurationListener(this);
        else if (kcalFrag != null)
            kcalFrag.SetCaloriesListener(this);
        else if (editIng != null)
            editIng.SetIngredientsListener(editIngredientListener, editingPosition);
        else if (editStep != null)
            editStep.SetStepListener(editMethodListener, editingPosition);
        else if (addURL != null)
            addURL.SetURLListener(this);

        editingPosition = -1;
    }

    /**
     * Setup the view for an ingredients card occupied with values
     */
    private void ShowRetractedIngredients() {

        Handler uiThread = new Handler(getMainLooper());

        binding.imvNoIngredients.setVisibility(View.GONE);
        binding.rvwNewIngredients.setVisibility(View.VISIBLE);

        if (!ingredientsExpanded) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.rvwNewIngredients.getLayoutParams();
            params.height = getRecyclerviewRetractHeight(true);
            uiThread.postDelayed(() -> {
                    ToggleEditIngViews(false);
                    binding.rvwNewIngredients.setLayoutFrozen(true);
                }
            , 50);
        }
    }

    /**
     * Setup the view for an step card occupied with values
     */
    private void ShowRetractedSteps() {

        Handler uiThread = new Handler(getMainLooper());

        binding.imvNoMethod.setVisibility(View.GONE);
        binding.rvwNewSteps.setVisibility(View.VISIBLE);

        if (!methodExpanded) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.rvwNewSteps.getLayoutParams();
            params.height = getRecyclerviewRetractHeight(false);
            uiThread.postDelayed(() -> {
                    ToggleEditMethViews(false);
                    binding.rvwNewSteps.setLayoutFrozen(true);
                }
            , 50);
        }
    }

    @Override
    public void onBackPressed() {
        if (ingredientsExpanded)
            RetractIngredientsCard();
        else if (methodExpanded)
            RetractMethodCard();
        else if (photoSheetExpanded)
            ClosePhotoSheet();
        else if (fabMenuOpen)
            AnimateFabMenu(binding.fabAddMenu);
        else {
            CheckCloseActivity();
        }
    }

    /**
     * If recipe empty then just close activity, if not then show 'Are you sure' alert dialog before
     * closing
     */
    private void CheckCloseActivity() {
        if (isRecipeEmpty() || !isAdding)
            CloseActivity();
        else {
            AlertDialog verification = new AlertDialog.Builder(this)
                    .setMessage(R.string.are_you_sure_discard)
                    .setPositiveButton(R.string.dialog_yes, (dialogInterface, i) -> CloseActivity())
                    .setNegativeButton(R.string.dialog_no, (dialogInterface, i) -> {})
                    .create();
            verification.show();
        }
    }

    private void CloseActivity() {
        super.onBackPressed();
        FileUtils.ClearDir(photosDirPath);
    }

    /**
     * Check if any information has been entered in a recipe
     */
    private boolean isRecipeEmpty() {
        return binding.etxtRecipeName.getText().length() == 0 &&
                recipeDuration == 0 &&
                recipeKcalPerPerson == 0 &&
                newIngredients.size() == 0 &&
                newSteps.size() == 0 &&
                currentImagePath == null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_activity_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_add_confirm:
                VerifyRecipe();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Verifies that inserted recipe is valid before proceeding to save
     */
    private void VerifyRecipe() {
        //Hide keyboard

        String title = binding.etxtRecipeName.getText().toString();

        if (title.isEmpty()) {
            Snackbar.make(binding.cdlyRoot, R.string.snackbar_no_title, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        else if (StringUtils.isStringAllWhitespace(title)) {
            Snackbar.make(binding.cdlyRoot, R.string.snackbar_title_invalid, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if (newIngredients.isEmpty()) {
            Snackbar.make(binding.cdlyRoot, R.string.snackbar_no_ingredients, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if (newSteps.isEmpty()) {
            Snackbar.make(binding.cdlyRoot, R.string.snackbar_no_steps, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        SaveRecipe();
    }

    /**
     * Packages up all relevant data, starts AsyncTask to save recipe and ends activity
     */
    private void SaveRecipe() {

        String title = binding.etxtRecipeName.getText().toString();

        //Prepare data for saving
        Recipe recipe = new Recipe.RecipeBuilder(isAdding ? -1 : editRecipeId, title)
                .imageFilePath(currentImagePath)
                .timeInMins(recipeDuration)
                .calories(recipeKcalPerPerson)
                .build();

        Bundle saveData = new Bundle();
        saveData.putParcelable(SAVE_TASK_RECIPE, recipe);
        saveData.putParcelableArrayList(SAVE_TASK_INGREDIENTS, new ArrayList<>(newIngredients));
        saveData.putParcelableArrayList(SAVE_TASK_METHOD, new ArrayList<>(newSteps));

        saveData.putBoolean(SAVE_TASK_IS_IMAGE_CAMERA, isImageFromCam);
        saveData.putString(SAVE_TASK_CAMERA_DIR_PATH, photosDirPath);

        if (isAdding)
            new SaveRecipeTask(getApplicationContext()).execute(saveData);
        else {
            //Add extra data needed here
            saveData.putParcelableArrayList(SAVE_TASK_OLD_INGREDIENTS, new ArrayList<>(oldIngredients));
            saveData.putParcelableArrayList(SAVE_TASK_OLD_METHOD, new ArrayList<>(oldSteps));

            new UpdateRecipeTask(getApplicationContext()).execute(saveData);
        }

        supportFinishAfterTransition();
    }



    @Override
    public boolean onSupportNavigateUp() {
        CheckCloseActivity();
        return true;
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (photoSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                binding.includeImageSheet.addImage.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    ClosePhotoSheet();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Setup image enter/exit animation in toolbar
     */
    private void SetupLayoutAnimator() {
        ObjectAnimator slideDown = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.slide_down_animator);
        ObjectAnimator slideUp = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.slide_up_animator);

        LayoutTransition imageSlider = new LayoutTransition();
        imageSlider.setAnimator(LayoutTransition.APPEARING, slideDown);
        imageSlider.setAnimator(LayoutTransition.DISAPPEARING, slideUp);

        binding.llyToolbarContainer.setLayoutTransition(imageSlider);

        binding.clyAddContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        binding.ctlyIngredientsContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        binding.ctlyMethodContainer.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    /**
     * Animates fab menu to open/close
     * @param view The menu fab view
     */
    public void AnimateFabMenu(View view) {
        if (fabMenuOpen) {
            ViewCompat.animate(view)
                    .rotation(0.f)
                    .withLayer()
                    .setDuration(250L)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
            AnimateFabMenuItem(binding.fabAddKcal, 0);
            AnimateFabMenuItemTag(binding.txtAddKcal, 0);
            AnimateFabMenuItem(binding.fabAddPhoto, 30);
            AnimateFabMenuItemTag(binding.txtAddPhoto, 30);
            AnimateFabMenuItem(binding.fabAddTime, 60);
            AnimateFabMenuItemTag(binding.txtAddTime, 60);
            fabMenuOpen = false;
        }
        else {
            ViewCompat.animate(view)
                    .rotation(135.f)
                    .withLayer()
                    .setDuration(250L)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
            AnimateFabMenuItem(binding.fabAddKcal, 0);
            AnimateFabMenuItemTag(binding.txtAddKcal, 0);
            AnimateFabMenuItem(binding.fabAddPhoto, 30);
            AnimateFabMenuItemTag(binding.txtAddPhoto, 30);
            AnimateFabMenuItem(binding.fabAddTime, 60);
            AnimateFabMenuItemTag(binding.txtAddTime, 60);
            fabMenuOpen = true;
        }
    }

    /**
     * Animates individual fab menu item both in/out of view
     * @param menuItem The menu fab view
     */
    private void AnimateFabMenuItem(View menuItem, int delay) {
        AnimationSet set = new AnimationSet(true);
        Animation rotate;

        float fabMenuXDelta = (binding.fabAddMenu.getX() + binding.fabAddMenu.getWidth() / 2)
                - (menuItem.getX() + menuItem.getWidth() / 2);
        float fabMenuYDelta = (binding.fabAddMenu.getY() + binding.fabAddMenu.getHeight() / 2)
                - (menuItem.getY() + menuItem.getHeight() / 2);

        if (fabMenuOpen) {
            rotate = new RotateAnimation(0.f, -150.f,
                    fabMenuXDelta + binding.fabAddMenu.getWidth() / 2,
                    fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            set.addAnimation(rotate);
            menuItem.setClickable(false);
        }
        else {
            Animation rotateBounce;

            rotate = new RotateAnimation(-150.f, 10.f,
                    fabMenuXDelta + binding.fabAddMenu.getWidth() / 2,
                    fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            rotateBounce = new RotateAnimation(0.f, -10.f,
                    fabMenuXDelta + binding.fabAddMenu.getWidth() / 2,
                    fabMenuYDelta + binding.fabAddMenu.getHeight() / 2);
            rotateBounce.setStartOffset(200);
            rotateBounce.setDuration(500);

            set.addAnimation(rotate);
            set.addAnimation(rotateBounce);

            menuItem.setClickable(true);
        }

        set.setDuration(150);
        set.setStartOffset(delay);
        set.setFillAfter(true);

        menuItem.startAnimation(set);
    }

    /**
     * Animates individual fab menu item tags both in/out of view
     * @param tag The menu tag view
     */
    private void AnimateFabMenuItemTag(TextView tag, int delay) {
        AnimationSet set = new AnimationSet(true);
        Animation translate;
        Animation fade;

        if (fabMenuOpen) {
            translate = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,  -200, Animation.RELATIVE_TO_SELF, 0);
            fade = new AlphaAnimation(1.f, 0.f);
            set.addAnimation(translate);
            set.addAnimation(fade);
        }
        else {
            translate = new TranslateAnimation(-200, 0, 0, 0);
            fade = new AlphaAnimation(0.f, 1.f);
            set.addAnimation(translate);
            set.addAnimation(fade);
        }

        set.setDuration(200);
        set.setStartOffset(delay);
        set.setFillAfter(true);

        tag.startAnimation(set);
    }

    /**
     * Open the photo bottom sheet
     */
    public void addImage(View view) {
        photoSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Remove image from the recipe
     */
    public void RemoveImage(View view) {
        HideImage();
        currentImagePath = null;
        isImageFromCam = false;
    }

    /**
     * Open the dialog fragment to add an image url
     */
    public void AddImageURL(View view) {
        AddImageURLFragment addUrlFrag= new AddImageURLFragment();
        addUrlFrag.SetURLListener(this);

        addUrlFrag.show(getFragmentManager(), FRAG_TAG_IMAGE_URL);
    }
    @Override
    public void onURLSet(String url) {
        SetImage(url);
    }

    /**
     * Open gallery selection bottom sheet
     */
    public void AddImageGallery(View view) {

        int response = PermissionsHandler.AskForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, PERM_REQ_CODE_READ_EXTERNAL);

        switch (response) {
            case PermissionsHandler.PERMISSION_GRANTED:
                OpenGallery();
                break;
            case PermissionsHandler.PERMISSION_DENIED:
                ClosePhotoSheet();
                break;
        }
    }

    /**
     * Initialise and show gallery selection bottom sheet
     */
    private void OpenGallery() {
        BSImagePicker addGalleryFrag = new BSImagePicker.Builder(getString(R.string.gallery_provider_authority))
                .setSpanCount(3)
                .setGridSpacing(0)
                .setPeekHeight(Utility.dpToPx(500))
                .hideCameraTile()
                .hideGalleryTile()
                .build();

        addGalleryFrag.show(getSupportFragmentManager(), FRAG_TAG_IMAGE_GALLERY);
    }

    @Override
    public void onSingleImageSelected(Uri uri) {
        SetImage(uri.getPath());
    }

    /**
     * Open camera activity to take a photo
     */
    public void AddImageCamera(View view) {

        int response = PermissionsHandler.AskForPermission(
                this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERM_REQ_CODE_CAMERA);

        switch (response) {
            case PermissionsHandler.PERMISSION_GRANTED:
                OpenCamera();
                break;
            case PermissionsHandler.PERMISSION_DENIED:
                ClosePhotoSheet();
                break;
        }
    }

    /**
     * Load and show a recipe image from a path
     * @param path The path to the image (either URL or local filepath)
     */
    private void SetImage(String path) {
        if (StringUtils.isStringAllWhitespace(path))
            return;

        isImageFromCam = false;

        currentImagePath = path;
        binding.setImagePath(path);
        binding.executePendingBindings();
        photoSheetExpanded = false;
        ClosePhotoSheet();

        ShowImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null)
            photosDirPath = data.getStringExtra(PHOTOS_DIRECTORY_PATH);

        switch(requestCode) {
            case ACT_REQ_CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    if (currentImagePath != null)
                        FileUtils.DeleteFile(currentImagePath);

                    SetImage(data.getStringExtra(CAMERA_PHOTO_PATH));
                    isImageFromCam = true;
                }
                break;
        }
    }

    @Override
    public void onCaloriesSet(int kcal) {
        binding.butKcal.setVisibility(View.VISIBLE);

        String kcalFormatted = getResources().getString(R.string.txt_kcal_per_person, kcal);
        binding.txtKcal.setText(kcalFormatted);

        recipeKcalPerPerson = kcal;
    }

    @Override
    public void onDurationSet(int minutes) {
        binding.butTime.setVisibility(View.VISIBLE);
        binding.txtTime.setText(StringUtils.minsToHourMins(minutes));
        recipeDuration = minutes;
    }

    private EditIngredientFragment.onIngredientEditedListener editIngredientListener = new EditIngredientFragment.onIngredientEditedListener() {
        @Override
        public void onIngredientEdited(Ingredient editedIngredient, int position) {

            if (newIngredients.get(position).equals(editedIngredient))
                return;

            if (!StringUtils.isStringAllWhitespace(editedIngredient.getIngredientText())) {
                newIngredients.get(position).setIngredientText(editedIngredient.getIngredientText());
                newIngredients.get(position).setQuantity(editedIngredient.getQuantity());
                newIngredients.get(position).setMeasurement(editedIngredient.getMeasurement());
                ingAdapter.notifyItemChanged(position);
                editingPosition = -1;
            }
        }
    };
    private EditMethodStepFragment.onStepEditedListener editMethodListener = new EditMethodStepFragment.onStepEditedListener() {
        @Override
        public void onStepEdited(String stepText, int position) {

            if (!StringUtils.isStringAllWhitespace(stepText)) {
                newSteps.get(position).setStepText(stepText);
                methAdapter.notifyItemChanged(position);
                editingPosition = -1;
            }
        }
    };

    private void ClosePhotoSheet() {
        photoSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERM_REQ_CODE_CAMERA:
                if (grantResults.length > 0) {
                    for (int result: grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            ClosePhotoSheet();
                            return;
                        }
                    }

                    new Handler(getMainLooper()).postDelayed(this::OpenCamera, 10);
                }
                break;
            case PERM_REQ_CODE_READ_EXTERNAL:
                if (grantResults.length > 0) {
                    for (int result: grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            ClosePhotoSheet();
                            return;
                        }
                    }

                    OpenGallery();
                }
                break;
        }
    }

    /**
     * Open the camera activity to take a photo
     */
    private void OpenCamera() {

        Intent openCamera = new Intent(this, CameraActivity.class);
        if (Utility.atLeastLollipop()) {
            Pair<View, String> navBar = Pair.create(findViewById(android.R.id.navigationBarBackground),
                    Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);

            if (navBar.first != null)
                ActivityCompat.startActivityForResult(this, openCamera, ACT_REQ_CODE_CAMERA,
                        ActivityOptions.makeSceneTransitionAnimation(this, navBar).toBundle());
            else
                ActivityCompat.startActivityForResult(this, openCamera, ACT_REQ_CODE_CAMERA,
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }
        else
            startActivityForResult(openCamera, ACT_REQ_CODE_CAMERA);
    }

    /**
     * Show the ImageView holding a recipe photo and animate the change
     */
    private void ShowImage() {
        binding.imvImageContainer.setVisibility(View.VISIBLE);
        binding.tbarAdd.setElevation(Utility.dpToPx(10));
        binding.clyAddTbar.setElevation(10);
    }
    /**
     * Hide the ImageView holding a recipe photo and animate the change
     */
    private void HideImage() {
        binding.imvImageContainer.setVisibility(View.GONE);
        binding.tbarAdd.setElevation(Utility.dpToPx(10));
        binding.clyAddTbar.setElevation(Utility.dpToPx(10));
    }

    /**
     * Opens dialog for user to enter number of kcal
     */
    public void AddKcal(View view) {
        CaloriesPickerFragment kcalFrag = new CaloriesPickerFragment();
        kcalFrag.SetCaloriesListener(this);
        kcalFrag.show(getFragmentManager(), FRAG_TAG_KCAL);
    }

    /**
     * Opens dialog for user to enter duration to make recipe
     */
    public void AddTime(View view) {
        DurationPickerFragment timeFrag = new DurationPickerFragment();
        timeFrag.SetDurationListener(this);

        timeFrag.show(getFragmentManager(), FRAG_TAG_TIME);
    }

    /**
     * Resets duration value and removes view
     */
    public void RemoveDuration(View view) {
        binding.butTime.setVisibility(View.GONE);
        recipeDuration = 0;
    }
    /**
     * Resets kcal value and removes view
     */
    public void RemoveKcal(View view) {
        binding.butKcal.setVisibility(View.GONE);
        recipeKcalPerPerson = 0;
    }

    /**
     * To achieve 'cutoff' effect for cards, must manually set the height of each RecyclerView to
     * their fully expanded height. As this isn't known until the card is expanded it can be calculated
     * based on what we know about the layout of the view (xml).
     * @param isIngredients Is this the ingredients card (calculations are different)
     * @return The height to set the RecyclerView
     */
    private int getRecyclerviewRetractHeight(boolean isIngredients) {

        ConstraintLayout.LayoutParams cardParams = (ConstraintLayout.LayoutParams)binding.crdvIngredients.getLayoutParams();
        ConstraintLayout.LayoutParams recyclerviewParams = (ConstraintLayout.LayoutParams)binding.rvwNewIngredients.getLayoutParams();

        int rootHeight = binding.cdlyRoot.getHeight();
        int cardTopMargin = cardParams.topMargin;
        int cardBottomMargin = cardParams.bottomMargin;
        int recyclerTopMargin = recyclerviewParams.topMargin;
        int recyclerBottomMargin = recyclerviewParams.bottomMargin;
        int recyclerPaddingTop = binding.rvwNewIngredients.getPaddingTop();
        int recyclerPaddingBottom = binding.rvwNewIngredients.getPaddingBottom();

        //Add button is View.GONE so must manually provide height here
        int AddButtonHeight = isIngredients ? Utility.dpToPx(47) : 0;

        return  rootHeight -
                cardTopMargin - cardBottomMargin -
                recyclerTopMargin - recyclerBottomMargin -
                recyclerPaddingTop - recyclerPaddingBottom -
                AddButtonHeight;
    }

    /**
     * Expand the ingredients card to full size of the screen while removing all other views in the
     * layout and animating everything
     */
    private void ExpandIngredientsCard() {

        if (photoSheetExpanded)
            return;
        else if (methodExpanded)
            return;

        Utility.setKeyboardVisibility(this, false);

        //Do this first then do rest after its done
        binding.crdvIngredients.setElevation(Utility.dpToPx(10));

        binding.imvNoIngredients.setVisibility(View.GONE);
        binding.rvwNewIngredients.setVisibility(View.VISIBLE);
        if (newIngredients.isEmpty())
            binding.txtNoIngredients.setVisibility(View.VISIBLE);

        binding.crdvMethod.setVisibility(View.GONE);
        binding.llyToolbarContainer.setVisibility(View.GONE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.GONE);

        binding.clyIngredientsInput.setVisibility(View.VISIBLE);

        AnimateOutFabMenu();

        ingredientsExpanded = true;

        binding.rvwNewIngredients.setLayoutFrozen(false);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewIngredients.getLayoutParams();
        params.height = 0;

        ToggleEditIngViews(true);
    }

    /**
     * Retract the ingredients card to full size of the screen while re-adding all other views in the
     * layout and animating everything
     */
    private void RetractIngredientsCard() {

        if (newIngredients.isEmpty()) {
            binding.imvNoIngredients.setVisibility(View.VISIBLE);
            binding.rvwNewIngredients.setVisibility(View.GONE);
            binding.txtNoIngredients.setVisibility(View.GONE);
        }

        //Scroll to first ingredient and then freeze recylerview (as its in retracted state)
        binding.rvwNewIngredients.smoothScrollToPosition(0);

        //If RecyclerView already scrolled to the top
        if (((LinearLayoutManager) binding.rvwNewIngredients.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition() == 0)
            binding.rvwNewIngredients.setLayoutFrozen(true);
        else {
            binding.rvwNewIngredients.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.rvwNewIngredients.setLayoutFrozen(true);
                        binding.rvwNewIngredients.removeOnScrollListener(this);
                    }
                }
            });
        }

        binding.llyToolbarContainer.setVisibility(View.VISIBLE);
        binding.crdvMethod.setVisibility(View.VISIBLE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.VISIBLE);

        binding.clyIngredientsInput.setVisibility(View.GONE);

        AnimateInFabMenu();

        ingredientsExpanded = false;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewIngredients.getLayoutParams();
        params.height = getRecyclerviewRetractHeight(true);

        //Fade out all non-essential views
        ToggleEditIngViews(true);

        //Reset elevation AFTER size reduction to avoid toolbar and card cross-fading
        final LayoutTransition transition = binding.ctlyIngredientsContainer.getLayoutTransition();
        transition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {}

            @Override
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                binding.crdvIngredients.setElevation(Utility.dpToPx(3));
                transition.removeTransitionListener(this);
            }
        });
    }

    /**
     * While expanded, all card RecyclerView items have remove buttons and drag handles. To save space,
     * this method removes them when the card is retracted.
     * @param shouldAnimate Should the views animate out or instantly change
     */
    private void ToggleEditIngViews(boolean shouldAnimate) {
        for (int i = 0; i < newIngredients.size(); ++i) {
            IngredientsAddAdapter.IngredientViewHolder holder = (IngredientsAddAdapter.IngredientViewHolder)
                    binding.rvwNewIngredients.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                if (shouldAnimate)
                    TransitionManager.beginDelayedTransition((ViewGroup)holder.itemView,
                            TransitionInflater.from(this).inflateTransition(R.transition.ingredient_card));

                holder.holderBinding.setCanEdit(ingredientsExpanded);
            }
        }
    }

    /**
     * Expand the method card to full size of the screen while removing all other views in the
     * layout and animating everything
     */
    private void ExpandMethodCard() {
        if (photoSheetExpanded)
            return;
        else if (ingredientsExpanded)
            return;

        Utility.setKeyboardVisibility(this, false);

        //Do this first then do rest after its done
        binding.crdvMethod.setElevation(Utility.dpToPx(10));

        binding.imvNoMethod.setVisibility(View.GONE);
        binding.rvwNewSteps.setVisibility(View.VISIBLE);
        if (newSteps.isEmpty())
            binding.txtNoSteps.setVisibility(View.VISIBLE);

        binding.crdvIngredients.setVisibility(View.GONE);
        binding.llyToolbarContainer.setVisibility(View.GONE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.GONE);

        binding.etxtAddStep.setVisibility(View.VISIBLE);
        binding.butAddStep.setVisibility(View.VISIBLE);

        AnimateOutFabMenu();

        methodExpanded = true;

        binding.rvwNewSteps.setLayoutFrozen(false);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewSteps.getLayoutParams();
        params.height = 0;

        ToggleEditMethViews(true);
    }

    /**
     * Expand the method card to full size of the screen while re-adding all other views in the
     * layout and animating everything
     */
    private void RetractMethodCard() {
        if (newSteps.isEmpty()) {
            binding.imvNoMethod.setVisibility(View.VISIBLE);
            binding.rvwNewSteps.setVisibility(View.GONE);
            binding.txtNoSteps.setVisibility(View.GONE);
        }

        //Scroll to first ingredient and then freeze recylerview (as its in retracted state)
        binding.rvwNewSteps.smoothScrollToPosition(0);

        //If RecyclerView already scrolled to the top
        if (((LinearLayoutManager) binding.rvwNewSteps.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition() == 0)
            binding.rvwNewSteps.setLayoutFrozen(true);
        else {
            binding.rvwNewSteps.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.rvwNewSteps.setLayoutFrozen(true);
                        binding.rvwNewSteps.removeOnScrollListener(this);
                    }
                }
            });
        }

        binding.llyToolbarContainer.setVisibility(View.VISIBLE);
        binding.crdvIngredients.setVisibility(View.VISIBLE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.VISIBLE);

        binding.etxtAddStep.setVisibility(View.GONE);
        binding.butAddStep.setVisibility(View.GONE);

        AnimateInFabMenu();

        methodExpanded = false;

        //Fade out all non-essential views
        ToggleEditMethViews(true);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewSteps.getLayoutParams();
        params.height = getRecyclerviewRetractHeight(false);

        //Reset elevation AFTER size reduction to avoid toolbar and card cross-fading
        final LayoutTransition transition = binding.ctlyMethodContainer.getLayoutTransition();
        transition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {}

            @Override
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                binding.crdvMethod.setElevation(Utility.dpToPx(3));
                transition.removeTransitionListener(this);
            }
        });
    }

    /**
     * While expanded, all card RecyclerView items have remove buttons and drag handles. To save space,
     * this method removes them when the card is retracted.
     * @param shouldAnimate Should the views animate out or instantly change
     */
    private void ToggleEditMethViews(boolean shouldAnimate) {
        for (int i = 0; i < newSteps.size(); ++i) {
            MethodStepAddAdapter.StepViewHolder holder = (MethodStepAddAdapter.StepViewHolder)
                    binding.rvwNewSteps.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                if (shouldAnimate)
                    TransitionManager.beginDelayedTransition((ViewGroup)holder.itemView,
                            TransitionInflater.from(this).inflateTransition(R.transition.method_card));

                holder.holderBinding.setCanEdit(methodExpanded);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void AnimateInFabMenu() {

        Slide anim = new Slide();
        anim.addTarget(binding.fabAddMenu);

        TransitionManager.beginDelayedTransition(binding.cdlyRoot, anim);
        binding.fabAddMenu.setVisibility(View.VISIBLE);
    }

    @SuppressLint("RestrictedApi")
    private void AnimateOutFabMenu() {
        Slide anim = new Slide();
        anim.addTarget(binding.fabAddMenu);

        TransitionManager.beginDelayedTransition(binding.cdlyRoot, anim);
        binding.fabAddMenu.setVisibility(View.INVISIBLE);
        if (fabMenuOpen)
            AnimateFabMenu(binding.fabAddMenu);
    }

    /**
     * Expands ingredient card
     */
    public void ViewIngredients(View view) {
        if (!ingredientsExpanded)
            ExpandIngredientsCard();
    }

    /**
     * Expands method card
     */
    public void ViewMethod(View view) {
        if (!methodExpanded)
            ExpandMethodCard();
    }

    /**
     * Adds a new ingredient to the list
     */
    public void AddIngredient(View view) {
        String ingredientName = binding.etxtAddIngredientName.getText().toString();

        if (StringUtils.isStringAllWhitespace(ingredientName))
            return;

        String quantityStr = binding.etxtAddIngredientQuantity.getText().toString();
        String measurement = binding.spnIngredientMeasurement.getSelectedItem().toString();
        int quantity;
        Ingredient temp;
        if (!quantityStr.isEmpty()) {
            quantity = Integer.parseInt(quantityStr);
            temp = new Ingredient(ingredientName, quantity, measurement);
        }
        else
            temp = new Ingredient(ingredientName, "");

        newIngredients.add(temp);
        ingAdapter.notifyItemInserted(newIngredients.size() - 1);
        binding.rvwNewIngredients.smoothScrollToPosition(newIngredients.size() - 1);

        binding.etxtAddIngredientName.setText("");
        binding.etxtAddIngredientQuantity.setText("");
        binding.spnIngredientMeasurement.setSelection(0);

        if (newIngredients.size() == 1)
            binding.txtNoIngredients.setVisibility(View.GONE);
    }

    /**
     * Adds a new method step to the list
     */
    public void AddMethodStep(View view) {
        String stepText = binding.etxtAddStep.getText().toString();

        if (StringUtils.isStringAllWhitespace(stepText))
            return;

        MethodStep temp = new MethodStep(stepText, newSteps.size() + 1);
        newSteps.add(temp);
        methAdapter.notifyItemInserted(newSteps.size() - 1);
        binding.rvwNewSteps.smoothScrollToPosition(newSteps.size() - 1);

        binding.etxtAddStep.setText("");

        if (newSteps.size() == 1)
            binding.txtNoSteps.setVisibility(View.GONE);
    }

    /**
     * Adapter for list of Ingredients in a newly added recipe
     */
    class IngredientsAddAdapter
            extends RecyclerView.Adapter<IngredientsAddAdapter.IngredientViewHolder>
            implements ItemTouchHelperAdapter {

        private OnStartDragListener startDragListener;

        void setStartDragListener(OnStartDragListener startDragListener) {
            this.startDragListener = startDragListener;
        }

        @NonNull
        @Override
        public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            AddIngredientItemBinding binding = AddIngredientItemBinding.inflate(inflater, parent, false);
            return new IngredientViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull final IngredientViewHolder holder, final int position) {
            Ingredient ingredient = newIngredients.get(position);
            holder.bind(ingredient);

            holder.holderBinding.imvIngredientDragHandle.setOnTouchListener((view, motionEvent) -> {
                    view.performClick();
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && startDragListener != null)
                        startDragListener.onStartDrag(holder);
                    return false;
                }
            );

            holder.holderBinding.setCanEdit(ingredientsExpanded);

            holder.holderBinding.getRoot().setOnClickListener((view) -> {

                    int pos = holder.getAdapterPosition();

                    if (ingredientsExpanded) {
                        EditIngredientFragment editIngFrag = new EditIngredientFragment();
                        editingPosition = pos;
                        editIngFrag.SetIngredientsListener(editIngredientListener, pos);

                        Bundle args = new Bundle();
                        args.putParcelable(INGREDIENT_OBJECT, newIngredients.get(pos));
                        editIngFrag.setArguments(args);

                        editIngFrag.show(getFragmentManager(), FRAG_TAG_EDIT_INGREDIENT);
                    }
                    else
                        ExpandIngredientsCard();
                }
            );

            //Enable remove button functionality
            holder.holderBinding.imbRemoveIngredient.setOnClickListener((view) -> {

                    int pos = holder.getAdapterPosition();

                    if (ingredientsExpanded) {
                        newIngredients.remove(pos);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos, newIngredients.size());

                        if (newIngredients.isEmpty())
                            binding.txtNoIngredients.setVisibility(View.VISIBLE);
                    }
                    else
                        ExpandIngredientsCard();
                }
            );
        }

        @Override
        public int getItemCount() {
            if (newIngredients == null)
                return 0;
            return newIngredients.size();
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            Utility.onRecyclerViewItemMoved(newIngredients, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position) {
            newIngredients.remove(position);
            notifyItemRemoved(position);
        }

        /**
         * ViewHolder for editable ingredient item
         */
        class IngredientViewHolder extends RecyclerView.ViewHolder {

            AddIngredientItemBinding holderBinding;

            IngredientViewHolder(AddIngredientItemBinding binding) {
                super(binding.getRoot());
                this.holderBinding = binding;
            }

            public void bind(Ingredient item) {
                holderBinding.setIngredient(item);
                holderBinding.executePendingBindings();
            }
        }
    }

    /**
     * Adapter for list of Method Steps in a newly added recipe
     */
    class MethodStepAddAdapter
            extends RecyclerView.Adapter<MethodStepAddAdapter.StepViewHolder>
            implements ItemTouchHelperAdapter {

        private OnStartDragListener startDragListener;

        void setStartDragListener(OnStartDragListener startDragListener) {
            this.startDragListener = startDragListener;
        }

        @NonNull
        @Override
        public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            AddMethodItemBinding binding = AddMethodItemBinding.inflate(inflater, parent, false);
            return new StepViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull final StepViewHolder holder, final int position) {
            MethodStep step = newSteps.get(position);
            holder.bind(step);

            holder.holderBinding.imvStepDragHandle.setOnTouchListener((view, motionEvent) -> {
                    view.performClick();
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && startDragListener != null)
                        startDragListener.onStartDrag(holder);
                    return false;
                }
            );

            holder.holderBinding.setCanEdit(methodExpanded);

            holder.holderBinding.getRoot().setOnClickListener((view) -> {
                    if (methodExpanded) {
                        EditMethodStepFragment editStepFrag = new EditMethodStepFragment();
                        editingPosition = position;
                        editStepFrag.SetStepListener(editMethodListener, position);

                        Bundle args = new Bundle();
                        args.putString(METHOD_STEP_OBJECT, newSteps.get(position).getStepText());
                        editStepFrag.setArguments(args);

                        editStepFrag.show(getFragmentManager(), FRAG_TAG_EDIT_STEP);
                    }
                    else
                        ExpandMethodCard();
                }
            );

            //Enable remove button functionality
            holder.holderBinding.imbRemoveStep.setOnClickListener((view) -> {
                    if (methodExpanded) {

                        onItemDismiss(position);

                        if (newSteps.isEmpty())
                            binding.txtNoSteps.setVisibility(View.VISIBLE);
                    }
                    else
                        ExpandMethodCard();
                }
            );
        }

        @Override
        public int getItemCount() {
            if (newSteps == null)
                return 0;
            return newSteps.size();
        }

        @Override
        public void onItemMove(final int fromPosition, final int toPosition) {

            Utility.onRecyclerViewItemMoved(newSteps, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position) {
            newSteps.remove(position);
            notifyItemRemoved(position);

            //Update the step numbers for the rest of the list
            for (int i = position; i < newSteps.size(); ++i)
                newSteps.get(i).setStepNumber(i + 1);

            notifyItemRangeChanged(position, newSteps.size());
        }

        /**
         * ViewHolder for editable method step item
         */
        class StepViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            AddMethodItemBinding holderBinding;

            StepViewHolder(AddMethodItemBinding binding) {
                super(binding.getRoot());
                this.holderBinding = binding;
            }

            public void bind(MethodStep item) {
                holderBinding.setMethodStep(item);
                holderBinding.executePendingBindings();
            }

            @Override
            public void onItemClear() {
                //Update view numbers
                MethodStep step;
                for (int i = 0; i < newSteps.size(); ++i) {
                    step = newSteps.get(i);
                    if (step.getStepNumber() != i + 1) {
                        step.setStepNumber(i + 1);
                        notifyItemChanged(i);
                    }
                }
            }
        }
    }
}
