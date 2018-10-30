package com.danthecodinggui.recipes.view.activity_home;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.transition.Slide;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.BR;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityHomeBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardBasicBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardComplexBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardPhotoBasicBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardPhotoComplexBinding;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.AnimUtils;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.Utility;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperAdapter;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchSwipeHelper;
import com.danthecodinggui.recipes.view.Loaders.GetRecipesLoader;
import com.danthecodinggui.recipes.view.activity_add_recipe.AddRecipeActivity;
import com.danthecodinggui.recipes.view.activity_view_recipe.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

import static com.danthecodinggui.recipes.msc.GlobalConstants.IMAGE_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.GlobalConstants.PREF_FILE_NAME;
import static com.danthecodinggui.recipes.msc.GlobalConstants.PREF_KEY_HOME_SORT_DIR;
import static com.danthecodinggui.recipes.msc.GlobalConstants.PREF_KEY_HOME_SORT_ORDER;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_BUNDLE;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_OBJECT;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SORT_ORDER_ALPHABETICAL;
import static com.danthecodinggui.recipes.msc.GlobalConstants.SORT_ORDER_TIME_ADDED;
import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;
import static com.danthecodinggui.recipes.msc.LogTags.GLIDE;

/**
 * Display all stored recipes
 */
public class HomeActivity extends AppCompatActivity
        implements Utility.PermissionDialogListener {

    ActivityHomeBinding binding;

    private List<Recipe> recipesList;
    private RecipesViewAdapter recipesAdapter;
    private ItemTouchHelper recipesTouchHelper;

    private ActionMode actionMode;

    //Flag determines if app can show local images (does the app have read external permission)
    private boolean noLocalImage = false;

    //Other flags
    private boolean searchOpen = false;
    private boolean restoringState = false;
    private boolean transitioningActivity = false;
    private boolean inActionMode = false;
    private boolean sortBySheetExpanded = false;
    private int currentSortOrder = SORT_ORDER_ALPHABETICAL;
    private boolean isSortAsc = true;

    private String lastSearchFilter;

    private GetRecipesLoader recipesLoader;

    SharedPreferences homePrefs;

    private BottomSheetBehavior sortBySheetBehaviour;

    //Loader IDs
    private static final int LOADER_RECIPE_PREVIEWS = 101;

    //Permission request codes
    private static final int REQ_CODE_READ_EXTERNAL = 201;

    //Instance state IDs
    private static final String SEARCH_OPEN = "SEARCH_OPEN";
    private static final String SEARCH_FILTER = "SEARCH_FILTER";
    private static final String IN_ACTION_MODE = "IN_ACTION_MODE";
    private static final String ACTION_MODE_SELECTION = "ACTION_MODE_SELECTION";
    private static final String SORT_BY_SHEET_OPEN = "SORT_BY_SHEET_OPEN";
    private static final String CURRENT_SORT_ORDER = "CURRENT_SORT_ORDER";
    private static final String IS_SORT_ASC = "IS_SORT_ASC";

    //TODO remove later
    private boolean inserting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.HomeTheme);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        //Conditionally set RecyclerView layout manager depending on screen orientation
        //Also ensure that landscape layout isn't used in split screen mode
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && !Utility.isMultiWindow(this))
            binding.rvwRecipes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        else
            binding.rvwRecipes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        recipesList = new ArrayList<>();

        //Setup RecyclerView Adapter
        recipesAdapter = new RecipesViewAdapter(null);
        binding.rvwRecipes.setAdapter(recipesAdapter);

        //Setup ItemTouchHelper
        ItemTouchHelper.Callback itemTouchCallback = new HomeItemTouchHelperCallback(recipesAdapter);
        recipesTouchHelper = new ItemTouchHelper(itemTouchCallback);
        recipesTouchHelper.attachToRecyclerView(binding.rvwRecipes);

        //Setup RecyclerView Animations
        ScaleInAnimator animator = new ScaleInAnimator(new OvershootInterpolator(1.f));
        animator.setAddDuration(200);
        animator.setRemoveDuration(200);
        animator.setChangeDuration(200);
        animator.setMoveDuration(200);
        binding.rvwRecipes.setItemAnimator(animator);

        //Show/hide floating action button on recyclerview scroll
        binding.rvwRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.fabAddRecipe.getVisibility() == View.VISIBLE) {
                    binding.fabAddRecipe.hide();
                } else if (dy < 0 && binding.fabAddRecipe.getVisibility() != View.VISIBLE) {
                    binding.fabAddRecipe.show();
                }
            }
        });

        setSupportActionBar(binding.tbarHome);

        //Setup sort by photo sheet
        sortBySheetBehaviour = BottomSheetBehavior.from(binding.includeSortSheet.sortBy);
        sortBySheetBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    sortBySheetExpanded = true;
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    sortBySheetExpanded = false;
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        homePrefs = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        currentSortOrder = GetCurrentSortOrder();
        isSortAsc = GetCurrentSortDir();

        SetSortOrderView(currentSortOrder);

        if (!inserting)
            getSupportLoaderManager().initLoader(LOADER_RECIPE_PREVIEWS, null, loaderCallbacks);
        else {
            String path = Environment.getExternalStorageDirectory().getPath();
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", false, false);
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", true, false);
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", false, true);
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", true, true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SEARCH_OPEN, searchOpen);
        outState.putString(SEARCH_FILTER, lastSearchFilter);
        outState.putBoolean(IN_ACTION_MODE, inActionMode);
        outState.putBoolean(SORT_BY_SHEET_OPEN, sortBySheetExpanded);
        outState.putInt(CURRENT_SORT_ORDER, currentSortOrder);
        outState.putBoolean(IS_SORT_ASC, isSortAsc);
        outState.putIntegerArrayList(ACTION_MODE_SELECTION, new ArrayList<>(recipesAdapter.GetSelection()));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        searchOpen = savedInstanceState.getBoolean(SEARCH_OPEN);
        lastSearchFilter = savedInstanceState.getString(SEARCH_FILTER);
        inActionMode = savedInstanceState.getBoolean(IN_ACTION_MODE);
        sortBySheetExpanded = savedInstanceState.getBoolean(SORT_BY_SHEET_OPEN);
        currentSortOrder = savedInstanceState.getInt(CURRENT_SORT_ORDER);
        isSortAsc = savedInstanceState.getBoolean(IS_SORT_ASC);

        if (inActionMode) {
            recipesAdapter.EnableActionMode();
            recipesAdapter.SetSelection(savedInstanceState.getIntegerArrayList(ACTION_MODE_SELECTION));
        }
        if (!isSortAsc)
            binding.includeSortSheet.imvSortDir.setImageDrawable(getDrawable(R.drawable.ic_sort_dir_desc));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_activity_toolbar, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        final MenuItem sortItem = menu.findItem(R.id.menu_sort_by);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(searchTextChangedListener);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Called when SearchView is collapsing
                if (searchItem.isActionViewExpanded()) {
                    AnimUtils.animateSearchToolbar(HomeActivity.this, binding.tbarHome, 3, false, false);
                    binding.txtSearchNoItems.setVisibility(View.INVISIBLE);
                    searchOpen = false;
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                AnimUtils.animateSearchToolbar(HomeActivity.this, binding.tbarHome, 3, false, true);
                searchOpen = true;
                return true;
            }
        });

        //Restore searchview state
        if (lastSearchFilter != null && !lastSearchFilter.isEmpty()) {
            restoringState = true;
            searchItem.expandActionView();
            searchView.setQuery(lastSearchFilter, false);
            searchView.clearFocus();
        }

        Handler uiThread = new Handler(getMainLooper());

        //Animate menu items in
        uiThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimateVectorDrawable(searchItem.getIcon());
            }
        }, 500);
        uiThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                AnimateVectorDrawable(sortItem.getIcon());
            }
        }, 1200);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_search:
                return true;
            case R.id.menu_sort_by:
                if (sortBySheetExpanded)
                    CloseSortBySheet();
                else
                    OpenSortBySheet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.home_activity_select_toolbar, menu);

            actionMode = mode;

            inActionMode = true;

            //Remove FAB
            Slide anim = new Slide();
            anim.addTarget(binding.fabAddRecipe);
            anim.setSlideEdge(Gravity.END);
            anim.setInterpolator(new AnticipateOvershootInterpolator(1.f));
            TransitionManager.beginDelayedTransition(binding.clyMainRoot, anim);
            binding.fabAddRecipe.setVisibility(View.INVISIBLE);

            recipesTouchHelper.attachToRecyclerView(null);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            switch (id) {
                case R.id.menu_delete_recipe:
                    recipesAdapter.DeleteSelection();
                    return true;
                case R.id.menu_select_all:
                    recipesAdapter.ToggleSelectAll();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            actionMode = null;
            inActionMode = false;

            recipesTouchHelper.attachToRecyclerView(binding.rvwRecipes);

            recipesAdapter.DisableActionMode();

            //Show FAB
            Slide anim = new Slide();
            anim.addTarget(binding.fabAddRecipe);
            anim.setSlideEdge(Gravity.END);
            anim.setInterpolator(new AnticipateOvershootInterpolator(1.f));
            TransitionManager.beginDelayedTransition(binding.clyMainRoot, anim);
            binding.fabAddRecipe.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onBackPressed() {
        if (sortBySheetExpanded)
            CloseSortBySheet();
        else
            super.onBackPressed();
    }

    /**
     * Gets the current sort order shared preference
     */
    private int GetCurrentSortOrder() {
        return homePrefs.getInt(PREF_KEY_HOME_SORT_ORDER, SORT_ORDER_ALPHABETICAL);
    }

    private boolean GetCurrentSortDir() {
        return homePrefs.getBoolean(PREF_KEY_HOME_SORT_DIR, true);
    }

    private void OpenSortBySheet() {
        sortBySheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    private void CloseSortBySheet() {
        sortBySheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event){
        //TODO can still click recipe cards when sheet open BUG
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (sortBySheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                binding.includeSortSheet.sortBy.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    CloseSortBySheet();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void SetSortOrderView(int sortOrder) {

        //Remove selected icon from all other options
        if (sortOrder == SORT_ORDER_ALPHABETICAL) {
            binding.includeSortSheet.btnSortByName.
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sheet_item_selected, 0);
            binding.includeSortSheet.btnSortByDate
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        else {
            binding.includeSortSheet.btnSortByDate
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sheet_item_selected, 0);
            binding.includeSortSheet.btnSortByName
                    .setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private SearchView.OnQueryTextListener searchTextChangedListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {

            if (restoringState && newText.isEmpty()) {
                restoringState = false;
                return true;
            }

            final List<Recipe> filteredRecipes = Filter(recipesList, newText);
            recipesAdapter.animateTo(filteredRecipes);

            lastSearchFilter = newText;

            //Show message if search shows no results
            if (filteredRecipes.size() == 0 && binding.txtNoItems.getVisibility() != View.VISIBLE) {
                AlphaAnimation anim = new AlphaAnimation(0.f, 1.0f);
                anim.setDuration(100);
                anim.setStartOffset(200);
                binding.txtSearchNoItems.startAnimation(anim);
                binding.txtSearchNoItems.setVisibility(View.VISIBLE);
            }
            else
                binding.txtSearchNoItems.setVisibility(View.INVISIBLE);
            return true;
        }
    };

    /**
     * Filters recipesList based on query from a searchview
     * @param items The original list of items/Recipes
     * @param query The text to search against
     * @return The filtered list
     */
    private List<Recipe> Filter(List<Recipe> items, String query) {
        query = query.toLowerCase();

        final List<Recipe> filteredRecipes = new ArrayList<>();
        for (Recipe r: items) {
            final String title = r.getTitle().toLowerCase();
            if (title.contains(query))
                filteredRecipes.add(r);
        }
        return filteredRecipes;
    }

    public void ToggleSortDir(View view) {

        final ImageView imageView = (ImageView) view;

        SetSortDirDrawable(imageView);

        AnimateVectorDrawable(imageView.getDrawable());

        isSortAsc = !isSortAsc;
        WriteNewSortDir();
    }

    /**
     * Animate an Animated Vector Drawable
     */
    private void AnimateVectorDrawable(Drawable drawable) {
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        }
        else if (drawable instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
            avd.start();
        }
    }

    private void SetSortDirDrawable(ImageView imageView) {
        //Change animated vector drawable
        if (isSortAsc)
            imageView.setImageDrawable(getDrawable(R.drawable.ic_sort_dir_asc));
        else
            imageView.setImageDrawable(getDrawable(R.drawable.ic_sort_dir_desc));
    }

    public void SortByName(View view) {

        if (currentSortOrder != SORT_ORDER_ALPHABETICAL) {
            currentSortOrder = SORT_ORDER_ALPHABETICAL;
            if (!isSortAsc)
                ToggleSortDir(binding.includeSortSheet.imvSortDir);

            SetSortOrderView(SORT_ORDER_ALPHABETICAL);
            WriteNewSortOrder();
        }
        CloseSortBySheet();
    }

    public void SortByDate(View view) {

        if (currentSortOrder != SORT_ORDER_TIME_ADDED) {
            currentSortOrder = SORT_ORDER_TIME_ADDED;
            if (isSortAsc)
                ToggleSortDir(binding.includeSortSheet.imvSortDir);

            SetSortOrderView(SORT_ORDER_TIME_ADDED);
            WriteNewSortOrder();
        }
        CloseSortBySheet();
    }

    private void WriteNewSortOrder() {
        SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putInt(PREF_KEY_HOME_SORT_ORDER, currentSortOrder);
        prefEditor.apply();
    }

    private void WriteNewSortDir() {
        SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putBoolean(PREF_KEY_HOME_SORT_DIR, isSortAsc);
        prefEditor.apply();
    }

    /**
     * Returns position of recipe in unfiltered list (needed for swipe-to-delete)
     */
    private int getRecipesPos(Recipe r) {
        for (int i = 0; i < recipesList.size(); ++i) {
            if (recipesList.get(i).equals(r))
                return i;
        }
        return -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        transitioningActivity = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQ_CODE_READ_EXTERNAL:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        noLocalImage = true;
                        Utility.showPermissionDeniedSnackbar(binding.clyMainRoot);
                    }
                    UnblockLoader();
                }
                break;
        }
    }
    @Override
    public void onFeatureDisabled() {
        noLocalImage = true;
        UnblockLoader();
    }

    /**
     * Allow the loader to continue loading recipes now that noLocalImage state has been determined
     */
    private void UnblockLoader() {
        recipesLoader.onPermissionResponse();
    }

    private LoaderManager.LoaderCallbacks<List<Recipe>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<Recipe>>() {
        @NonNull
        @Override
        public Loader<List<Recipe>> onCreateLoader(int id, @Nullable Bundle args) {
            Handler uiThread = new Handler(getMainLooper());

            recipesLoader = new GetRecipesLoader(HomeActivity.this, uiThread,
                    new GetRecipesLoader.ImagePermissionsListener() {
                        @Override
                        public void onImagePermRequested() {
                            int response = PermissionsHandler.AskForPermission(HomeActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL);

                            if (response == PermissionsHandler.PERMISSION_GRANTED)
                                UnblockLoader();
                            else if (response == PermissionsHandler.PERMISSION_DENIED)
                                onFeatureDisabled();

                        }
                    }, currentSortOrder, isSortAsc);

            homePrefs.registerOnSharedPreferenceChangeListener(recipesLoader);
            return recipesLoader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<Recipe>> loader, List<Recipe> loadedRecipes) {
            if (loadedRecipes.size() == 0)
                binding.txtNoItems.setVisibility(View.VISIBLE);
            else
                binding.txtNoItems.setVisibility(View.INVISIBLE);

            recipesList = loadedRecipes;
            if (searchOpen)
                searchTextChangedListener.onQueryTextChange(lastSearchFilter);
            else
                recipesAdapter.UpdateRecords(loadedRecipes);

            Log.v(DATA_LOADING, "Load complete");
        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<Recipe>> loader) {
            homePrefs.unregisterOnSharedPreferenceChangeListener(recipesLoader);

            recipesAdapter.UpdateRecords(Collections.<Recipe>emptyList());
        }
    };

    private interface ActionModeSelection {
        void EnableActionMode();
        void DisableActionMode();
        void onItemSelected(int position);
        void onItemDeselected(int position);
        void ToggleSelectAll();
        void DeleteSelection();
        List<Integer> GetSelection();
        void SetSelection(List<Integer> newSelection);
        boolean isItemSelected(int position);
    }

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipesViewAdapter
            extends RecyclerView.Adapter<RecipesViewAdapter.RecipeViewHolder>
            implements ItemTouchHelperAdapter, ActionModeSelection {

        private List<Recipe> displayedRecipesList;

        //ViewHolder type flags
        private final int
                BASIC = 0,
                COMPLEX = 1,
                PHOTO_BASIC = 2,
                PHOTO_COMPLEX = 3;

        private List<Integer> selectedItems;

        //Flag stating whether toggle all button pressed when all items selected
        //(Used to stop action mode closing with size 0 in this use case)
        private boolean selectAllNone = false;

        RecipesViewAdapter(List<Recipe> list) {
            if (list != null)
                displayedRecipesList = new ArrayList<>(list);
            else
                displayedRecipesList = new ArrayList<>();

            selectedItems = new ArrayList<>();
        }

        @Override
        public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case COMPLEX:
                    return new ComplexViewHolder(
                            RecipeCardComplexBinding.inflate(inflater, parent, false),
                            this);
                case PHOTO_BASIC:
                    return new BasicPhotoViewHolder(
                            RecipeCardPhotoBasicBinding.inflate(inflater, parent, false),
                            this);
                case PHOTO_COMPLEX:
                    return new ComplexPhotoViewHolder(
                            RecipeCardPhotoComplexBinding.inflate(inflater, parent, false),
                            this);
                default:
                    return new BasicViewHolder(
                            RecipeCardBasicBinding.inflate(inflater, parent, false),
                            this);
            }
        }

        @Override
        public void onBindViewHolder(RecipeViewHolder holder, int pos) {

            Recipe recipe = displayedRecipesList.get(pos);

            holder.bind(recipe);

            holder.binding.setVariable(BR.isSelected, isItemSelected(holder.getAdapterPosition()));
        }

        @Override
        public int getItemViewType(int position) {

            Recipe item = displayedRecipesList.get(position);

            boolean isComplex = item.hasExtendedInfo();
            boolean hasPhoto = item.hasPhoto() && !noLocalImage && Utility.imageExists(item.getImagePath());

            //Nullify path to ensure android doesn't try and animate image in shared transition
            if (!hasPhoto)
                item.setImagePath(null);

            if (isComplex && hasPhoto)
                return PHOTO_COMPLEX;
            else if (hasPhoto)
                return PHOTO_BASIC;
            else if (isComplex)
                return COMPLEX;
            else
                return BASIC;
        }

        @Override
        public int getItemCount() {
            if (displayedRecipesList == null)
                return 0;
            return displayedRecipesList.size();
        }

        /**
         * Update adapter's own list of shown recipe records and animate to new list
         * @param updatedRecords
         */
        void UpdateRecords(List<Recipe> updatedRecords) {
            if (updatedRecords != null) {
                if (displayedRecipesList == null || displayedRecipesList.isEmpty()) {
                    displayedRecipesList = new ArrayList<>(updatedRecords);
                    notifyItemRangeInserted(0, updatedRecords.size());
                }
                else
                    animateTo(updatedRecords);
            }
        }

        //Methods allowing animation between two lists of recipes
        void animateTo(List<Recipe> items) {
            applyAndAnimateRemovals(items);
            applyAndAnimateAdditions(items);
            applyAndAnimateMovedItems(items);
        }
        private void applyAndAnimateRemovals(List<Recipe> filteredList) {
            for (int i = displayedRecipesList.size() - 1; i >= 0; --i) {
                final Recipe item = displayedRecipesList.get(i);
                if (!filteredList.contains(item))
                    removeItem(i);
            }
        }
        private void applyAndAnimateAdditions(List<Recipe> filteredList) {
            for (int i = 0, count = filteredList.size(); i < count; ++i) {
                final Recipe item = filteredList.get(i);
                if (!displayedRecipesList.contains(item))
                    addItem(i, item);
            }
        }
        private void applyAndAnimateMovedItems(List<Recipe> filteredList) {
            for (int toPos = filteredList.size() - 1; toPos >= 0; --toPos) {
                final Recipe item = filteredList.get(toPos);
                final int fromPos = displayedRecipesList.indexOf(item);
                if (fromPos >= 0 && fromPos != toPos)
                    moveItem(fromPos, toPos);
            }
        }
        Recipe removeItem(int pos) {
            final Recipe item = displayedRecipesList.remove(pos);
            notifyItemRemoved(pos);
            return item;
        }
        void addItem(int pos, Recipe item) {
            displayedRecipesList.add(pos, item);
            notifyItemInserted(pos);
        }
        void moveItem(int fromPos, int toPos) {
            final Recipe item = displayedRecipesList.remove(fromPos);
            displayedRecipesList.add(toPos, item);
            notifyItemMoved(fromPos, toPos);
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            //Not Supported Here
        }

        @Override
        public void onItemDismiss(final int position) {

            //TODO Bug, position can't be final as changes in/out of search

            //Need to delete recipe from both filtered/unfiltered list
            final int unfilteredPos = getRecipesPos(displayedRecipesList.get(position));

            //Now we have positions, can delete from both lists
            final Recipe recipeToDelete = removeItem(position);
            recipesList.remove(unfilteredPos);

            ShowDeletedSnackbar(false,
                    new Runnable() {
                        @Override
                        public void run() {
                            recipesList.add(unfilteredPos, recipeToDelete);
                            addItem(position, recipeToDelete);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            new DeleteRecipeTask(getApplicationContext()).execute(
                                    Collections.singletonList(recipeToDelete.getRecipeId())
                            );
                        }
                    }
            );
        }

        @Override
        public void EnableActionMode() {
            startSupportActionMode(actionModeCallback);
        }

        @Override
        public void DisableActionMode() {
            if (actionMode != null)
                actionMode.finish();

            //Reset all still-selected items
            if (selectedItems.size() != 0) {
                for (int i = 0; i < selectedItems.size(); ++i) {
                    RecipeViewHolder holder = (RecipeViewHolder) binding.rvwRecipes.findViewHolderForAdapterPosition(selectedItems.get(i));
                    if (holder != null) {
                        CardView card = (CardView) holder.binding.getRoot();
                        card.setCardBackgroundColor(Color.WHITE);
                    }
                    else
                        selectedItems.remove(i);
                }
                selectedItems.clear();
            }
        }

        @Override
        public void onItemSelected(int position) {
            selectedItems.add(position);
            InvalidateActionModeTitle();
        }
        @Override
        public void onItemDeselected(int position) {
            selectedItems.remove((Integer)position);
            if (selectedItems.size() == 0) {
                if (selectAllNone)
                    selectAllNone = false;
                else
                    DisableActionMode();
            }
            InvalidateActionModeTitle();
        }

        @Override
        public void ToggleSelectAll() {

            boolean isAllSelected = selectedItems.size() == displayedRecipesList.size();
            if (isAllSelected)
                selectAllNone = true;

            for (int i = 0; i < displayedRecipesList.size(); ++i) {
                if (selectedItems.contains(i) && !isAllSelected)
                    continue;

                RecipeViewHolder holder = (RecipeViewHolder) binding.rvwRecipes.findViewHolderForAdapterPosition(i);
                if (holder != null)
                    holder.ToggleActionModeSelected();
                else
                    onItemSelected(i);
            }
        }

        @Override
        public void DeleteSelection() {
            final List<Recipe> recipesToDelete = new ArrayList<>();
            final List<Integer> unfilteredRecipePositions = new ArrayList<>();

            Recipe removeTemp;
            int unfilteredPosTemp;
            for (Integer position: selectedItems) {
                unfilteredPosTemp = getRecipesPos(displayedRecipesList.get(position));
                unfilteredRecipePositions.add(unfilteredPosTemp);
            }
            for (int i = 0; i < selectedItems.size(); ++i) {
                removeTemp = removeItem(selectedItems.get(i) - i);

                recipesToDelete.add(removeTemp);
                recipesList.remove(unfilteredRecipePositions.get(i) - i);
            }

            ShowDeletedSnackbar(recipesToDelete.size() > 1,
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < selectedItems.size(); ++i) {
                                addItem(selectedItems.get(i), recipesToDelete.get(i));
                                recipesList.add(unfilteredRecipePositions.get(i), recipesToDelete.get(i));
                            }
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            List<Long> recipePrimaryKeys = new ArrayList<>();
                            for (Recipe recipe: recipesToDelete)
                                recipePrimaryKeys.add(recipe.getRecipeId());

                            new DeleteRecipeTask(getApplicationContext()).execute(recipePrimaryKeys);
                        }
                    }
            );

        }

        @Override
        public List<Integer> GetSelection() {
            return selectedItems;
        }

        @Override
        public void SetSelection(List<Integer> newSelection) {
            selectedItems = newSelection;
            InvalidateActionModeTitle();
        }

        private void InvalidateActionModeTitle() {
            if (actionMode != null)
                actionMode.setTitle(getResources().getString(R.string.action_mode_title, selectedItems.size()));
        }

        private void ShowDeletedSnackbar(boolean isMultiDelete, final Runnable onActionClicked, final Runnable onDismissed) {
            Snackbar.make(binding.clyMainRoot,
                    isMultiDelete ? R.string.multiple_recipes_removed : R.string.recipe_removed,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Handler(getMainLooper()).post(onActionClicked);
                        }
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            //Start AsyncTask to delete recipe
                            if (event != DISMISS_EVENT_ACTION)
                                new Handler(getMainLooper()).post(onDismissed);
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.snackbar_action_default))
                    .show();
        }

        @Override
        public boolean isItemSelected(int position) {
            return selectedItems.contains(position);
        }

        /**
         * Base class for all 4 types of recipe cards
         */
        class RecipeViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener, ItemTouchSwipeHelper {

            ViewDataBinding binding;
            private ActionModeSelection actionModeSelection;

            RecipeViewHolder(View itemView, ActionModeSelection actionModeSelection) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                this.actionModeSelection = actionModeSelection;
            }

            public void bind(Recipe item) {
                binding.setVariable(BR.recipe, item);
                binding.executePendingBindings();
            }

            @Override
            public void onClick(View view) {
                if (inActionMode)
                    ToggleActionModeSelected();
                else
                    ViewRecipe(displayedRecipesList.get(getAdapterPosition()), null);
            }

            @Override
            public boolean onLongClick(View view) {
                if (!inActionMode) {
                    actionModeSelection.EnableActionMode();
                    ToggleActionModeSelected();
                }
                return true;
            }

            void ToggleActionModeSelected() {
                CardView card = binding.getRoot().findViewById(R.id.crd_root);

                int pos = getAdapterPosition();
                if (actionModeSelection.isItemSelected(pos)) {
                    actionModeSelection.onItemDeselected(pos);
                    card.setCardBackgroundColor(Color.WHITE);
                }
                else {
                    actionModeSelection.onItemSelected(pos);
                    card.setCardBackgroundColor(getResources().getColor(R.color.colorCardSelected));
                }
            }

            @Override
            public void onItemSwipe(float percentSwiped) {

                if (inActionMode)
                    return;

                CardView card = binding.getRoot().findViewById(R.id.crd_root);

                float alteredPercentSwiped = percentSwiped * 1.1f;
                if (alteredPercentSwiped > 1)
                    alteredPercentSwiped = 1;


                card.setCardBackgroundColor(Utility.interpolateRGB(0xffffff, 0xff0000, alteredPercentSwiped));
                if (percentSwiped == 0 || percentSwiped == 1)
                    card.setCardBackgroundColor(Color.WHITE);
            }
        }

        /**
         * Recipe with no optional extra information
         */
        class BasicViewHolder extends RecipeViewHolder {

            BasicViewHolder(RecipeCardBasicBinding itemBinding, ActionModeSelection actionModeSelection) {
                super(itemBinding.getRoot(), actionModeSelection);
                binding = itemBinding;
            }
        }
        /**
         * Recipe with no photo but with kcal and/or time data
         */
        class ComplexViewHolder extends RecipeViewHolder {

            ComplexViewHolder(RecipeCardComplexBinding itemBinding, ActionModeSelection actionModeSelection) {
                super(itemBinding.getRoot(), actionModeSelection);
                binding = itemBinding;
            }
        }

        /**
         * Recipe with photo but no other optional info
         */
        class BasicPhotoViewHolder extends RecipeViewHolder {

            RecipeCardPhotoBasicBinding photoBinding;

            BasicPhotoViewHolder(RecipeCardPhotoBasicBinding itemBinding, ActionModeSelection actionModeSelection) {
                super(itemBinding.getRoot(), actionModeSelection);
                binding = photoBinding = itemBinding;
            }

            @Override
            public void bind(Recipe item) {
                super.bind(item);

                //Set unique transition name for this specific card
                ViewCompat.setTransitionName(photoBinding.ivwCrdPreview,
                        getString(R.string.main_card_transition_name) + "_" +
                                item.getTitle() + "_" + Integer.toString(getAdapterPosition()));

                photoBinding.setVariable(BR.imageLoadedCallback, new RequestListener<Drawable>() {

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        Log.e(GLIDE, "Data Binding image loading failed (from filepath)", e);
                        return false;
                    }
                });
            }

            @Override
            public void onClick(View view) {
                if (inActionMode)
                    ToggleActionModeSelected();
                else
                    ViewRecipe(displayedRecipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
            }
        }
        /**
         * Recipe with photo and kcal and/or time data
         */
        class ComplexPhotoViewHolder extends RecipeViewHolder {

            RecipeCardPhotoComplexBinding photoBinding;

            ComplexPhotoViewHolder(RecipeCardPhotoComplexBinding itemBinding, ActionModeSelection actionModeSelection) {
                super(itemBinding.getRoot(), actionModeSelection);
                binding = photoBinding = itemBinding;
            }

            @Override
            public void bind(Recipe item) {
                super.bind(item);

                //Set unique transition name for this specific card
                ViewCompat.setTransitionName(photoBinding.ivwCrdPreview,
                        getString(R.string.main_card_transition_name) + "_" +
                                item.getTitle() + "_" + Integer.toString(getAdapterPosition()));

                photoBinding.setVariable(BR.imageLoadedCallback, new RequestListener<Drawable>() {

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        Log.e(GLIDE, "Data Binding image loading failed (from filepath)", e);
                        return false;
                    }
                });
            }

            @Override
            public void onClick(View view) {
                if (inActionMode)
                    ToggleActionModeSelected();
                else
                    ViewRecipe(displayedRecipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
            }
        }
    }

    private void ViewRecipe(Recipe recipe, ImageView sharedImageView) {

        //To ensure a card can only be clicked once
        if (transitioningActivity)
            return;

        getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.main_activity_view_exit));
        getWindow().setReenterTransition(TransitionInflater.from(this).inflateTransition(R.transition.main_activity_view_reenter));

        Intent viewRecipe = new Intent(this, ViewRecipeActivity.class);

        Bundle recipeBundle = new Bundle();
        recipeBundle.putParcelable(RECIPE_DETAIL_OBJECT, recipe);

        viewRecipe.putExtra(RECIPE_DETAIL_BUNDLE, recipeBundle);

        transitioningActivity = true;

        ActivityOptions options;

        if (Utility.atLeastLollipop()) {

            Pair<View, String> navBar = Pair.create(findViewById(android.R.id.navigationBarBackground),
                    Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);

            if (sharedImageView != null) {

                //To have both the image and navbar as shared elements, must both pass Pairs and manually
                //set transition name for image
                viewRecipe.putExtra(IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));

                Pair<View, String> image = Pair.create((View) sharedImageView, ViewCompat.getTransitionName(sharedImageView));

                if (navBar.first != null)
                    options = ActivityOptions.makeSceneTransitionAnimation(this, image, navBar);
                else
                    options = ActivityOptions.makeSceneTransitionAnimation(this, image);

            }
            else {
                if (navBar.first != null)
                    options = ActivityOptions.makeSceneTransitionAnimation(this, navBar);
                else
                    options = ActivityOptions.makeSceneTransitionAnimation(this);
            }
            startActivity(viewRecipe, options.toBundle());
            return;
        }

        //Should just start activity normally
        startActivity(viewRecipe);
    }

    public void AddRecipe(View view) {
        Intent addRecipe = new Intent(getApplicationContext(), AddRecipeActivity.class);

        if (Utility.atLeastLollipop()) {

            getWindow().setExitTransition(null);
            getWindow().setReenterTransition(null);

            //ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            //startActivity(addRecipe, options.toBundle());
            startActivity(addRecipe);
        }
        else
            startActivity(addRecipe);
    }
}