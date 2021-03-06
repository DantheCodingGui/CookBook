package com.danthecodinggui.recipes.view.activity_home;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
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
import com.danthecodinggui.recipes.msc.utility.AnimUtils;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.utility.FileUtils;
import com.danthecodinggui.recipes.msc.utility.Utility;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperAdapter;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchSwipeHelper;
import com.danthecodinggui.recipes.view.Loaders.GetRecipesLoader;
import com.danthecodinggui.recipes.view.activity_add_recipe.AddEditRecipeActivity;
import com.danthecodinggui.recipes.view.activity_view_recipe.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

import static com.danthecodinggui.recipes.msc.GlobalConstants.IMAGE_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_PREF_FILE_NAME;
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
        implements PermissionsHandler.PermissionDialogListener {

    ActivityHomeBinding binding;

    private List<Recipe> recipesList;
    private RecipesViewAdapter recipesAdapter;
    private ItemTouchHelper recipesTouchHelper;

    private Parcelable recyclerViewState;
    private int recyclerViewScroll;

    private ActionMode actionMode;

    //Flag determines if app can show local images (does the app have read external permission)
    private boolean noLocalImage = false;

    //Other flags
    private boolean searchOpen = false;
    private boolean restoringState = false;
    private boolean transitioningActivity = false;
    private boolean inActionMode = false;
    private boolean isSortSheetOpen = false;
    private int currentSortOrder = SORT_ORDER_ALPHABETICAL;
    private boolean isSortAsc = true;
    private boolean shouldShowMenuAnims = true;

    private String lastSearchFilter;

    private GetRecipesLoader recipesLoader;

    private SharedPreferences homePrefs;

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
    private static final String RECYCLERVIEW_STATE = "RECYCLERVIEW_STATE";
    private static final String RECYCLERVIEW_SCROLL = "RECYCLERVIEW_SCROLL";

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
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (inActionMode)
                    return;

                if (dy > 0 && binding.fabHomeAddRecipe .getVisibility() == View.VISIBLE) {
                    binding.fabHomeAddRecipe.hide();
                } else if (dy < 0 && binding.fabHomeAddRecipe.getVisibility() != View.VISIBLE) {
                    binding.fabHomeAddRecipe.show();
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
                    isSortSheetOpen = true;
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    isSortSheetOpen = false;
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        homePrefs = getSharedPreferences(RECIPE_PREF_FILE_NAME, Context.MODE_PRIVATE);

        currentSortOrder = GetCurrentSortOrder();
        isSortAsc = GetCurrentSortDir();

        SetSortOrderView(currentSortOrder);

        //Flip sort direction icon if not ASC
        if (!isSortAsc)
            binding.includeSortSheet.imvSortDir.setImageDrawable(
                    getDrawable(R.drawable.ic_sort_dir_desc));

        if (savedInstanceState != null) {
            recyclerViewState = savedInstanceState.getParcelable(RECYCLERVIEW_STATE);
            recyclerViewScroll = savedInstanceState.getInt(RECYCLERVIEW_SCROLL);
        }

        getSupportLoaderManager().initLoader(LOADER_RECIPE_PREVIEWS, null, loaderCallbacks);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SEARCH_OPEN, searchOpen);
        outState.putString(SEARCH_FILTER, lastSearchFilter);
        outState.putBoolean(IN_ACTION_MODE, inActionMode);
        outState.putBoolean(SORT_BY_SHEET_OPEN, isSortSheetOpen);
        outState.putInt(CURRENT_SORT_ORDER, currentSortOrder);
        outState.putBoolean(IS_SORT_ASC, isSortAsc);
        outState.putIntegerArrayList(ACTION_MODE_SELECTION, new ArrayList<>(recipesAdapter.GetSelection()));

        outState.putParcelable(RECYCLERVIEW_STATE, recyclerViewState);
        outState.putInt(RECYCLERVIEW_SCROLL, recyclerViewScroll);
    }

    @Override
    protected void onPause() {
        super.onPause();

        RecyclerView.LayoutManager layoutManager = binding.rvwRecipes.getLayoutManager();

        recyclerViewState = layoutManager.onSaveInstanceState();
        if (layoutManager instanceof LinearLayoutManager)
            recyclerViewScroll = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
        else
            recyclerViewScroll = ((StaggeredGridLayoutManager)layoutManager).findFirstVisibleItemPositions(null)[0];
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        searchOpen = savedInstanceState.getBoolean(SEARCH_OPEN);
        lastSearchFilter = savedInstanceState.getString(SEARCH_FILTER);
        inActionMode = savedInstanceState.getBoolean(IN_ACTION_MODE);
        isSortSheetOpen = savedInstanceState.getBoolean(SORT_BY_SHEET_OPEN);
        currentSortOrder = savedInstanceState.getInt(CURRENT_SORT_ORDER);
        isSortAsc = savedInstanceState.getBoolean(IS_SORT_ASC);

        if (inActionMode) {
            recipesAdapter.EnableActionMode();
            recipesAdapter.SetSelection(savedInstanceState.getIntegerArrayList(ACTION_MODE_SELECTION));
        }
        if (!isSortAsc)
            binding.includeSortSheet.imvSortDir.setImageDrawable(getDrawable(R.drawable.ic_sort_dir_desc));

        //Only show if activity open is fresh
        shouldShowMenuAnims = false;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
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
                    sortItem.setVisible(true);
                    searchOpen = false;
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                AnimUtils.animateSearchToolbar(HomeActivity.this, binding.tbarHome, 3, false, true);
                sortItem.setVisible(false);
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

        //Ensure animation doesn't run on orientation change
        if (shouldShowMenuAnims) {
            //Animate menu items in
            uiThread.postDelayed(() -> AnimUtils.animateVectorDrawable(searchItem.getIcon()), 300);
            uiThread.postDelayed(() -> AnimUtils.animateVectorDrawable(sortItem.getIcon()), 700);
        }
        else {
            AnimUtils.instaAnimateVectorDrawable(searchItem.getIcon());
            AnimUtils.instaAnimateVectorDrawable(sortItem.getIcon());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_search:
                return true;
            case R.id.menu_sort_by:
                if (isSortSheetOpen)
                    CloseSortBySheet();
                else
                    OpenSortBySheet();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @SuppressLint("RestrictedApi")
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.home_activity_select_toolbar, menu);

            actionMode = mode;

            inActionMode = true;

            //Remove FAB
            Slide anim = new Slide();
            anim.addTarget(binding.fabHomeAddRecipe);
            anim.setSlideEdge(Gravity.END);
            anim.setInterpolator(new AnticipateOvershootInterpolator(1.f));
            TransitionManager.beginDelayedTransition(binding.cdlyRoot, anim);
            binding.fabHomeAddRecipe.setVisibility(View.INVISIBLE);

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

        @SuppressLint("RestrictedApi")
        @Override
        public void onDestroyActionMode(ActionMode mode) {

            actionMode = null;
            inActionMode = false;

            recipesTouchHelper.attachToRecyclerView(binding.rvwRecipes);

            //If scroll was active and fab was hidden, if all recipes have been deleted, can't scroll
            //to re-show fab, so show here
            if (recipesList.isEmpty())
                binding.fabHomeAddRecipe.show();

            recipesAdapter.DisableActionMode();

            //Show FAB
            Slide anim = new Slide();
            anim.addTarget(binding.fabHomeAddRecipe);
            anim.setSlideEdge(Gravity.END);
            anim.setInterpolator(new AnticipateOvershootInterpolator(1.f));
            TransitionManager.beginDelayedTransition(binding.cdlyRoot, anim);
            binding.fabHomeAddRecipe.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onBackPressed() {
        if (isSortSheetOpen)
            CloseSortBySheet();
        else
            super.onBackPressed();
    }

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

    /**
     * Set the sort order indicator icon on whichever button is currently selected
     * @param sortOrder The current sort order
     */
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

    /**
     * Toggle the sort direction value and toggle/animate the bottom sheet icon
     */
    public void ToggleSortDir(View view) {

        final ImageView imageView = (ImageView) view;

        SetSortDirDrawable(imageView);

        AnimUtils.animateVectorDrawable(imageView.getDrawable());

        isSortAsc = !isSortAsc;
        WriteNewSortDir();
    }

    private void SetSortDirDrawable(ImageView imageView) {
        //Change animated vector drawable
        if (isSortAsc)
            imageView.setImageDrawable(getDrawable(R.drawable.ic_sort_dir_asc));
        else
            imageView.setImageDrawable(getDrawable(R.drawable.ic_sort_dir_desc));
    }

    /**
     * Set the new sort direction to sort alphabetically
     */
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

    /**
     * Set the new sort direction to sort based on newest added recipe
     */
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

    /**
     * Update the sort order shared preference
     */
    private void WriteNewSortOrder() {
        SharedPreferences pref = getSharedPreferences(RECIPE_PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putInt(PREF_KEY_HOME_SORT_ORDER, currentSortOrder);
        prefEditor.apply();
    }

    /**
     * Update the sort direction shared preference
     */
    private void WriteNewSortDir() {
        SharedPreferences pref = getSharedPreferences(RECIPE_PREF_FILE_NAME, Context.MODE_PRIVATE);
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

        recyclerViewState = null;
        recyclerViewScroll = 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQ_CODE_READ_EXTERNAL:
                if (grantResults.length > 0) {
                    for (int result: grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            noLocalImage = true;
                            break;
                        }
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
                    () -> {
                            int response = PermissionsHandler.AskForPermission(HomeActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL);

                            if (response == PermissionsHandler.PERMISSION_GRANTED)
                                UnblockLoader();
                            else if (response == PermissionsHandler.PERMISSION_DENIED)
                                onFeatureDisabled();

                        }
                    , currentSortOrder, isSortAsc);

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

            if (recyclerViewState != null) {
                binding.rvwRecipes.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                recyclerViewState = null;
                binding.rvwRecipes.scrollToPosition(recyclerViewScroll);
                recyclerViewScroll = 0;
            }

            Log.v(DATA_LOADING, "HomeActivity Recipes Load Complete");
        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<Recipe>> loader) {
            homePrefs.unregisterOnSharedPreferenceChangeListener(recipesLoader);

            recipesAdapter.UpdateRecords(Collections.<Recipe>emptyList());
        }
    };

    /**
     * Bridges adapter and surrounding activity in regards to action mode interactions
     */
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
        private int deleteSelectionSize = 0;

        RecipesViewAdapter(List<Recipe> list) {
            if (list != null)
                displayedRecipesList = new ArrayList<>(list);
            else
                displayedRecipesList = new ArrayList<>();

            selectedItems = new ArrayList<>();
        }

        @NonNull
        @Override
        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

            if (inActionMode)
                holder.binding.setVariable(BR.isSelected, isItemSelected(holder.getAdapterPosition()));
            else
                holder.binding.setVariable(BR.isSelected, false);
        }

        @Override
        public int getItemViewType(int position) {

            Recipe item = displayedRecipesList.get(position);

            boolean isComplex = item.hasExtendedInfo();
            boolean hasPhoto = item.hasPhoto() && !noLocalImage && FileUtils.ImageExists(item.getImagePath());

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
         * @param updatedRecords Updated records
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

            if (recipesList.isEmpty()) {
                TransitionManager.beginDelayedTransition(binding.cdlyRoot);
                binding.txtNoItems.setVisibility(View.VISIBLE);
            }

            ShowDeletedSnackbar(false,
                    () -> { recipesList.add(unfilteredPos, recipeToDelete);
                            addItem(position, recipeToDelete);

                            binding.txtNoItems.setVisibility(View.INVISIBLE);
                        }
                    ,
                    () -> new DeleteRecipeTask(getApplicationContext()).execute(
                                    Collections.singletonList(recipeToDelete.getRecipeId()))
            );

            //Prevents bug where user can't scroll recyclerview to make it re-appear
            binding.fabHomeAddRecipe.show();
        }

        @Override
        public void EnableActionMode() {
            startSupportActionMode(actionModeCallback);
            selectedItems.clear();
            InvalidateActionModeTitle();
        }

        @Override
        public void DisableActionMode() {
            if (actionMode != null)
                actionMode.finish();

            notifyDataSetChanged();
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

                if (selectedItems.contains(i))
                    onItemDeselected(i);
                else
                    onItemSelected(i);
            }

            notifyDataSetChanged();
        }

        @Override
        public void DeleteSelection() {

            if (selectedItems.isEmpty())
                return;

            final List<Recipe> recipesToDelete = new ArrayList<>();
            final List<Integer> unfilteredRecipePositions = new ArrayList<>();

            //First need to sort selected items so later removal offset doesn't cause crash
            Collections.sort(selectedItems);

            //Then record and make a list of all unfiltered positions (before they are removed)
            for (Integer position: selectedItems)
                unfilteredRecipePositions.add(getRecipesPos(displayedRecipesList.get(position)));

            //Then actually remove elements from both unfiltered and filtered recipes lists
            for (int i = 0; i < selectedItems.size(); ++i) {
                recipesToDelete.add(removeItem(selectedItems.get(i) - i));
                recipesList.remove(unfilteredRecipePositions.get(i) - i);
            }

            deleteSelectionSize = selectedItems.size();
            InvalidateActionModeTitle();

            if (recipesList.isEmpty()) {
                TransitionManager.beginDelayedTransition(binding.cdlyRoot);
                binding.txtNoItems.setVisibility(View.VISIBLE);
            }

            ShowDeletedSnackbar(recipesToDelete.size() > 1,
                    () -> {
                            for (int i = 0; i < selectedItems.size(); ++i) {
                                addItem(selectedItems.get(i), recipesToDelete.get(i));
                                recipesList.add(unfilteredRecipePositions.get(i), recipesToDelete.get(i));
                            }

                            binding.txtNoItems.setVisibility(View.INVISIBLE);

                            deleteSelectionSize = 0;
                            InvalidateActionModeTitle();
                        }
                    ,
                    () -> {
                            List<Long> recipePrimaryKeys = new ArrayList<>();
                            for (Recipe recipe: recipesToDelete)
                                recipePrimaryKeys.add(recipe.getRecipeId());

                            new DeleteRecipeTask(getApplicationContext()).execute(recipePrimaryKeys);

                            deleteSelectionSize = 0;
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
                actionMode.setTitle(getResources().getString(R.string.action_mode_title, selectedItems.size() - deleteSelectionSize));
        }

        /**
         * Shows a snackbar with 'UNDO' action when any combination of recipes are deleted
         * @param isMultiDelete Did multiple records get deleted
         * @param onActionClicked Action to take when action clcked
         * @param onDismissed Action to take when snackbar dismissed/times out
         */
        private void ShowDeletedSnackbar(boolean isMultiDelete, final Runnable onActionClicked, final Runnable onDismissed) {
            Snackbar.make(binding.cdlyRoot,
                    isMultiDelete ? R.string.multiple_recipes_removed : R.string.recipe_removed,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_undo, (view) -> new Handler(getMainLooper()).post(onActionClicked))
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
                if (isSortSheetOpen)
                    return;

                if (inActionMode)
                    ToggleActionModeSelected();
                else
                    ViewRecipe(displayedRecipesList.get(getAdapterPosition()), null);
            }

            @Override
            public boolean onLongClick(View view) {
                if (!inActionMode && deleteSelectionSize == 0) {
                    actionModeSelection.EnableActionMode();
                    ToggleActionModeSelected();
                }
                return true;
            }

            void ToggleActionModeSelected() {

                int pos = getAdapterPosition();
                if (actionModeSelection.isItemSelected(pos)) {
                    actionModeSelection.onItemDeselected(pos);
                    binding.setVariable(BR.isSelected, false);
                }
                else {
                    actionModeSelection.onItemSelected(pos);
                    binding.setVariable(BR.isSelected, true);
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

                card.setCardBackgroundColor(AnimUtils.interpolateRGB(0xffffff, 0xff0000, alteredPercentSwiped));
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
                if (isSortSheetOpen)
                    return;

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
                if (isSortSheetOpen)
                    return;

                if (inActionMode)
                    ToggleActionModeSelected();
                else
                    ViewRecipe(displayedRecipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
            }
        }
    }

    /**
     * Open activity to view a particular recipe
     * @param recipe The recipe being viewed
     * @param sharedImageView If recipe includes photo, the ImageView to run a shared element transition on
     */
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

    /**
     * Opens AddEditRecipeActivity to add a new recipe
     */
    public void AddRecipe(View view) {
        Intent addRecipe = new Intent(getApplicationContext(), AddEditRecipeActivity.class);

        if (Utility.atLeastLollipop()) {

            getWindow().setExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.main_activity_add));
            getWindow().setReenterTransition(TransitionInflater.from(this).inflateTransition(R.transition.main_activity_add));

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(addRecipe, options.toBundle());
        }
        else
            startActivity(addRecipe);
    }
}