package com.danthecodinggui.recipes.view.add_recipe;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.transition.Slide;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.transition.TransitionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;

import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityAddRecipeBinding;
import com.danthecodinggui.recipes.databinding.AddIngredientItemBinding;
import com.danthecodinggui.recipes.databinding.AddMethodItemBinding;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.StringUtils;
import com.danthecodinggui.recipes.msc.Utility;
import com.danthecodinggui.recipes.view.CameraActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.GlobalConstants.CAMERA_PHOTO_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.CAMERA_PHOTO_SAVED;
import static com.danthecodinggui.recipes.msc.GlobalConstants.INGREDIENT_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.METHOD_STEP_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.PHOTOS_DIRECTORY_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_CAMERA_DIR_PATH;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_INGREDIENTS;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_IS_IMAGE_CAMERA;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_METHOD;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SAVE_TASK_RECIPE;
import static com.danthecodinggui.recipes.msc.LogTags.CAMERA;

/**
 * Provides functionality to add_activity_toolbar recipes
 */
public class AddRecipeActivity extends AppCompatActivity implements
        CaloriesPickerFragment.onCaloriesSetListener,
        DurationPickerFragment.onDurationSetListener, AddImageURLFragment.onURLSetListener,
        BSImagePicker.OnSingleImageSelectedListener {

    ActivityAddRecipeBinding binding;

    private boolean isPortrait;

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
    private static final int PERM_REQ_CODE_WRITE_EXTERNAL = 203;

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

    private boolean ingredientsExpanded = false;
    private boolean methodExpanded = false;

    private int recipeDuration;
    private int recipeKcalPerPerson;
    private String currentImagePath;

    private String photosDirPath;
    private boolean isImageFromCam = false;

    private int editingPosition = -1;

    private BottomSheetBehavior photoSheetBehaviour;
    private boolean photoSheetExpanded = false;

    private List<Ingredient> newIngredients;
    private IngredientsAddAdapter ingAdapter;

    private List<MethodStep> newSteps;
    private MethodStepAddAdapter methAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe);

        //Setup toolbar
        setSupportActionBar(binding.tbarAdd);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            isPortrait = false;
        else {
            isPortrait = true;
            if (Utility.isMultiWindow(this)) {
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) binding.spcAdd.getLayoutParams();
                params.height = Utility.dpToPx(this, 1);
            }

        }

        SetupLayoutAnimator();

        newIngredients = new ArrayList<>();
        newSteps = new ArrayList<>();

        binding.rvwNewIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingAdapter = new IngredientsAddAdapter();
        binding.rvwNewIngredients.setAdapter(ingAdapter);

        binding.rvwNewSteps.setLayoutManager((new LinearLayoutManager(this)));
        methAdapter = new MethodStepAddAdapter();
        binding.rvwNewSteps.setAdapter(methAdapter);

        //Setup button enable/disable based on edittext contents
        binding.butAddIngredient.setEnabled(false);
        binding.etxtAddIngredient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Utility.CheckButtonEnabled(binding.butAddIngredient, charSequence.toString());
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

        //Setup add_activity_toolbar image photo sheet
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
        if (!newIngredients.isEmpty()) {
            binding.imvNoIngredients.setVisibility(View.GONE);
            binding.rvwNewIngredients.setVisibility(View.VISIBLE);
            binding.rvwNewIngredients.setLayoutFrozen(true);
        }
        newSteps = savedInstanceState.getParcelableArrayList(METHOD_LIST);
        if (!newSteps.isEmpty()) {
            binding.imvNoMethod.setVisibility(View.GONE);
            binding.rvwNewSteps.setVisibility(View.VISIBLE);
            binding.rvwNewSteps.setLayoutFrozen(true);
        }

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
            super.onBackPressed();
            //TODO remove later
            Utility.ClearDir(photosDirPath);
        }
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
            case R.id.menu_add_recipe:
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
            Snackbar.make(binding.cdlyAddRoot, R.string.snackbar_no_title, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        else if (Utility.isStringAllWhitespace(title)) {
            Snackbar.make(binding.cdlyAddRoot, R.string.snackbar_title_invalid, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if (newIngredients.isEmpty()) {
            Snackbar.make(binding.cdlyAddRoot, R.string.snackbar_no_ingredients, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if (newSteps.isEmpty()) {
            Snackbar.make(binding.cdlyAddRoot, R.string.snackbar_no_steps, Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        if (isImageFromCam) {
            int response = PermissionsHandler.AskForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    PERM_REQ_CODE_WRITE_EXTERNAL);

            if (response == PermissionsHandler.PERMISSION_GRANTED)
                SaveRecipe();
            else
                ;//HANDLE SOMEHOW
        }
        else
            SaveRecipe();
    }

    /**
     * Packages up all relevant data, starts AsyncTask to save recipe and ends activity
     */
    private void SaveRecipe() {

        String title = binding.etxtRecipeName.getText().toString();

        //Prepare data for saving
        Recipe recipe = new Recipe.RecipeBuilder(-1, title)
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

        //Start saving recipe
        new SaveRecipeTask(getApplicationContext()).execute(saveData);

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();

        //TODO remove later
        Utility.ClearDir(photosDirPath);
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
            AnimateFabItem(binding.fabAddPhoto);
            AnimateFabItem(binding.txtAddPhoto);
            AnimateFabItem(binding.fabAddTime);
            AnimateFabItem(binding.txtAddTime);
            AnimateFabItem(binding.fabAddKcal);
            AnimateFabItem(binding.txtAddKcal);
            fabMenuOpen = false;
        }
        else {
            ViewCompat.animate(view)
                    .rotation(135.f)
                    .withLayer()
                    .setDuration(250L)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
            AnimateFabItem(binding.fabAddPhoto);
            AnimateFabItem(binding.txtAddPhoto);
            AnimateFabItem(binding.fabAddTime);
            AnimateFabItem(binding.txtAddTime);
            AnimateFabItem(binding.fabAddKcal);
            AnimateFabItem(binding.txtAddKcal);
            fabMenuOpen = true;
        }
    }

    /**
     * Animates individual fab item both in/out of view
     * @param menuItem The menu fab view
     */
    private void AnimateFabItem(View menuItem) {
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

        set.setDuration(200);
        set.setFillAfter(true);

        menuItem.startAnimation(set);
    }

    public void addImage(View view) {
        photoSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    public void RemoveImage(View view) {
        HideImage();
        currentImagePath = null;
        isImageFromCam = false;
    }

    public void AddImageURL(View view) {
        AddImageURLFragment addUrlFrag= new AddImageURLFragment();
        addUrlFrag.SetURLListener(this);

        addUrlFrag.show(getFragmentManager(), FRAG_TAG_IMAGE_URL);
    }
    @Override
    public void onURLSet(String url) {
        SetImage(url);
    }

    public void AddImageGallery(View view) {

        int response = PermissionsHandler.AskForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, PERM_REQ_CODE_READ_EXTERNAL);

        switch (response) {
            case PermissionsHandler.PERMISSION_GRANTED:
                OpenGallery();
                break;
            case PermissionsHandler.PERMISSION_DENIED:
                ClosePhotoSheet();
                Utility.showPermissionReenableSnackbar(binding.cdlyAddRoot, "Storage");
                break;
        }
    }

    private void OpenGallery() {
        BSImagePicker addGalleryFrag = new BSImagePicker.Builder("com.danthecodinggui.fileprovider")
                .setSpanCount(3)
                .setGridSpacing(0)
                .setPeekHeight(Utility.dpToPx(this, 500))
                .hideCameraTile()
                .hideGalleryTile()
                .build();

        addGalleryFrag.show(getSupportFragmentManager(), FRAG_TAG_IMAGE_GALLERY);
    }

    @Override
    public void onSingleImageSelected(Uri uri) {
        SetImage(uri.getPath());
    }

    public void AddImageCamera(View view) {

        int response = PermissionsHandler.AskForPermission(this, Manifest.permission.CAMERA, PERM_REQ_CODE_CAMERA);

        switch (response) {
            case PermissionsHandler.PERMISSION_GRANTED:
                OpenCamera();
                break;
            case PermissionsHandler.PERMISSION_DENIED:
                ClosePhotoSheet();
                Utility.showPermissionReenableSnackbar(binding.cdlyAddRoot, "Camera");
                break;
        }
    }

    private void SetImage(String path) {
        if (Utility.isStringAllWhitespace(path))
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
                        Utility.DeleteFile(currentImagePath);

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
        public void onIngredientEdited(String ingredientName, int position) {
            if (!Utility.isStringAllWhitespace(ingredientName)) {
                newIngredients.get(position).setIngredientText(ingredientName);
                ingAdapter.notifyItemChanged(position);
                editingPosition = -1;
            }
        }
    };
    private EditMethodStepFragment.onStepEditedListener editMethodListener = new EditMethodStepFragment.onStepEditedListener() {
        @Override
        public void onStepEdited(String stepText, int position) {
            if (!Utility.isStringAllWhitespace(stepText)) {
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
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        new Handler(getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                OpenCamera();
                            }
                        }, 10);
                    else {
                        ClosePhotoSheet();
                        Utility.showPermissionDeniedSnackbar(binding.cdlyAddRoot);
                    }
                }
                break;
            case PERM_REQ_CODE_READ_EXTERNAL:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        OpenGallery();
                    else {
                        ClosePhotoSheet();
                        Utility.showPermissionDeniedSnackbar(binding.cdlyAddRoot);
                    }
                }
                break;
            case PERM_REQ_CODE_WRITE_EXTERNAL:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        SaveRecipe();
                    else {
                        //Handle somehow
                    }
                }
                break;
        }
    }

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

    private void ShowImage() {
        binding.imvImageContainer.setVisibility(View.VISIBLE);
        binding.tbarAdd.setElevation(Utility.dpToPx(this, 10));
        binding.clyAddTbar.setElevation(10);
    }
    private void HideImage() {
        binding.imvImageContainer.setVisibility(View.GONE);
        binding.tbarAdd.setElevation(Utility.dpToPx(this, 10));
        binding.clyAddTbar.setElevation(Utility.dpToPx(this, 10));
    }

    /**
     * Opens dialog for user to enter number of kcal
     * @param view
     */
    public void AddKcal(View view) {
        CaloriesPickerFragment kcalFrag = new CaloriesPickerFragment();
        kcalFrag.SetCaloriesListener(this);
        kcalFrag.show(getFragmentManager(), FRAG_TAG_KCAL);
    }

    /**
     * Opens dialog for user to enter duration to make recipe
     * @param view
     */
    public void AddTime(View view) {
        DurationPickerFragment timeFrag = new DurationPickerFragment();
        timeFrag.SetDurationListener(this);

        timeFrag.show(getFragmentManager(), FRAG_TAG_TIME);
    }

    /**
     * Resets duration value and removes view
     * @param view
     */
    public void RemoveDuration(View view) {
        binding.butTime.setVisibility(View.GONE);
        recipeDuration = 0;
    }
    /**
     * Resets kcal value and removes view
     * @param view
     */
    public void RemoveKcal(View view) {
        binding.butKcal.setVisibility(View.GONE);
        recipeKcalPerPerson = 0;
    }

    private void ExpandIngredientsCard() {

        if (photoSheetExpanded)
            return;
        else if (methodExpanded)
            return;

        //Do this first then do rest after its done
        binding.crdvIngredients.setElevation(Utility.dpToPx(this, 10));

        binding.imvNoIngredients.setVisibility(View.GONE);
        binding.rvwNewIngredients.setVisibility(View.VISIBLE);
        if (newIngredients.isEmpty())
            binding.txtNoIngredients.setVisibility(View.VISIBLE);

        binding.rvwNewIngredients.setLayoutFrozen(false);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewIngredients.getLayoutParams();
        params.height = 0;

        binding.crdvMethod.setVisibility(View.GONE);
        binding.llyToolbarContainer.setVisibility(View.GONE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.GONE);

        binding.etxtAddIngredient.setVisibility(View.VISIBLE);
        binding.butAddIngredient.setVisibility(View.VISIBLE);

        AnimateOutFabMenu();

        ingredientsExpanded = true;
    }

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
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.rvwNewIngredients.setLayoutFrozen(true);
                        binding.rvwNewIngredients.removeOnScrollListener(this);
                    }
                }
            });
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewIngredients.getLayoutParams();
        params.height = binding.rvwNewIngredients.getHeight();

        binding.llyToolbarContainer.setVisibility(View.VISIBLE);
        binding.crdvMethod.setVisibility(View.VISIBLE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.VISIBLE);

        binding.etxtAddIngredient.setVisibility(View.GONE);
        binding.butAddIngredient.setVisibility(View.GONE);


        //Reset elevation AFTER size reduction to avoid toolbar and card cross-fading
        final LayoutTransition transition = binding.ctlyIngredientsContainer.getLayoutTransition();
        transition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {}

            @Override
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                binding.crdvIngredients.setElevation(Utility.dpToPx(getApplicationContext(), 3));
                transition.removeTransitionListener(this);
            }
        });

        AnimateInFabMenu();

        ingredientsExpanded = false;
    }

    private void ExpandMethodCard() {
        if (photoSheetExpanded)
            return;
        else if (ingredientsExpanded)
            return;

        //Do this first then do rest after its done
        binding.crdvMethod.setElevation(Utility.dpToPx(this, 10));

        binding.imvNoMethod.setVisibility(View.GONE);
        binding.rvwNewSteps.setVisibility(View.VISIBLE);
        if (newSteps.isEmpty())
            binding.txtNoSteps.setVisibility(View.VISIBLE);

        binding.rvwNewSteps.setLayoutFrozen(false);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewSteps.getLayoutParams();
        params.height = 0;

        binding.crdvIngredients.setVisibility(View.GONE);
        binding.llyToolbarContainer.setVisibility(View.GONE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.GONE);

        binding.etxtAddStep.setVisibility(View.VISIBLE);
        binding.butAddStep.setVisibility(View.VISIBLE);

        AnimateOutFabMenu();

        methodExpanded = true;
    }

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
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        binding.rvwNewSteps.setLayoutFrozen(true);
                        binding.rvwNewSteps.removeOnScrollListener(this);
                    }
                }
            });
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.rvwNewSteps.getLayoutParams();
        params.height = binding.rvwNewSteps.getHeight();

        binding.llyToolbarContainer.setVisibility(View.VISIBLE);
        binding.crdvIngredients.setVisibility(View.VISIBLE);
        if (isPortrait)
            binding.spcAdd.setVisibility(View.VISIBLE);

        binding.etxtAddStep.setVisibility(View.GONE);
        binding.butAddStep.setVisibility(View.GONE);


        //Reset elevation AFTER size reduction to avoid toolbar and card cross-fading
        final LayoutTransition transition = binding.ctlyMethodContainer.getLayoutTransition();
        transition.addTransitionListener(new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {}

            @Override
            public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
                binding.crdvMethod.setElevation(Utility.dpToPx(getApplicationContext(), 3));
                transition.removeTransitionListener(this);
            }
        });

        AnimateInFabMenu();

        methodExpanded = false;
    }

    private void AnimateInFabMenu() {

        Slide anim = new Slide();
        anim.addTarget(binding.fabAddMenu);

        TransitionManager.beginDelayedTransition(binding.cdlyAddRoot, anim);
        binding.fabAddMenu.setVisibility(View.VISIBLE);
    }

    private void AnimateOutFabMenu() {
        Slide anim = new Slide();
        anim.addTarget(binding.fabAddMenu);

        TransitionManager.beginDelayedTransition(binding.cdlyAddRoot, anim);
        binding.fabAddMenu.setVisibility(View.INVISIBLE);
        if (fabMenuOpen)
            AnimateFabMenu(binding.fabAddMenu);
    }

    public void ViewIngredients(View view) {
        if (!ingredientsExpanded)
            ExpandIngredientsCard();
    }

    public void ViewMethod(View view) {
        if (!methodExpanded)
            ExpandMethodCard();
    }

    public void AddIngredient(View view) {
        String ingredientName = binding.etxtAddIngredient.getText().toString();

        if (Utility.isStringAllWhitespace(ingredientName))
            return;

        Ingredient temp = new Ingredient(ingredientName);
        newIngredients.add(temp);
        ingAdapter.notifyItemInserted(newIngredients.size() - 1);
        binding.rvwNewIngredients.smoothScrollToPosition(newIngredients.size() - 1);

        binding.etxtAddIngredient.setText("");

        if (newIngredients.size() == 1)
            binding.txtNoIngredients.setVisibility(View.GONE);
    }

    public void AddMethodStep(View view) {
        String stepText = binding.etxtAddStep.getText().toString();

        if (Utility.isStringAllWhitespace(stepText))
            return;

        MethodStep temp = new MethodStep(stepText, newSteps.size() + 1);
        newSteps.add(temp);
        methAdapter.notifyItemInserted(newSteps.size() - 1);
        binding.rvwNewSteps.smoothScrollToPosition(newSteps.size() - 1);

        binding.etxtAddStep.setText("");

        if (newSteps.size() == 1)
            binding.txtNoSteps.setVisibility(View.GONE);
    }

    //TODO: consolidate adapters for this and view ingredients (are basically the same)
    class IngredientsAddAdapter extends RecyclerView.Adapter<IngredientsAddAdapter.IngredientViewHolder> {

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            AddIngredientItemBinding binding = AddIngredientItemBinding.inflate(inflater, parent, false);
            return new IngredientViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, final int position) {
            Ingredient ingredient = newIngredients.get(position);
            holder.bind(ingredient);

            holder.holderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ingredientsExpanded) {
                        EditIngredientFragment editIngFrag = new EditIngredientFragment();
                        editingPosition = position;
                        editIngFrag.SetIngredientsListener(editIngredientListener, position);

                        Bundle args = new Bundle();
                        args.putParcelable(INGREDIENT_OBJECT, newIngredients.get(position));
                        editIngFrag.setArguments(args);

                        editIngFrag.show(getFragmentManager(), FRAG_TAG_EDIT_INGREDIENT);
                    }
                    else
                        ExpandIngredientsCard();
                }
            });

            //Enable remove button functionality
            holder.holderBinding.imbRemoveIngredient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ingredientsExpanded) {
                        newIngredients.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, newIngredients.size());

                        if (newIngredients.isEmpty())
                            binding.txtNoIngredients.setVisibility(View.VISIBLE);
                    }
                    else
                        ExpandIngredientsCard();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (newIngredients == null)
                return 0;
            return newIngredients.size();
        }

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

    class MethodStepAddAdapter extends RecyclerView.Adapter<MethodStepAddAdapter.StepViewHolder> {

        @Override
        public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            AddMethodItemBinding binding = AddMethodItemBinding.inflate(inflater, parent, false);
            return new StepViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(StepViewHolder holder, final int position) {
            MethodStep step = newSteps.get(position);
            holder.bind(step);

            holder.holderBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
            });

            //Enable remove button functionality
            holder.holderBinding.imbRemoveStep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (methodExpanded) {
                        newSteps.remove(position);
                        notifyItemRemoved(position);

                        //Update the step numbers for the rest of the list
                        for (int i = position; i < newSteps.size(); ++i)
                           newSteps.get(i).setStepNumber(i + 1);

                        notifyItemRangeChanged(position, newSteps.size());

                        if (newSteps.isEmpty())
                            binding.txtNoSteps.setVisibility(View.VISIBLE);
                    }
                    else
                        ExpandMethodCard();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (newSteps == null)
                return 0;
            return newSteps.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {

            AddMethodItemBinding holderBinding;

            StepViewHolder(AddMethodItemBinding binding) {
                super(binding.getRoot());
                this.holderBinding = binding;
            }

            public void bind(MethodStep item) {
                holderBinding.setMethodStep(item);
                holderBinding.executePendingBindings();
            }
        }
    }
}
