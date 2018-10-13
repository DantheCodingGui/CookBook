package com.danthecodinggui.recipes.view;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danthecodinggui.recipes.BR;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityMainBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardBasicBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardComplexBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardPhotoBasicBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardPhotoComplexBinding;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.AnimUtils;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.Utility;
import com.danthecodinggui.recipes.view.Loaders.GetRecipesLoader;
import com.danthecodinggui.recipes.view.add_recipe.AddRecipeActivity;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

import static com.danthecodinggui.recipes.msc.GlobalConstants.IMAGE_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_BUNDLE;
import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_OBJECT;
import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;
import static com.danthecodinggui.recipes.msc.LogTags.GLIDE;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity
        implements Utility.PermissionDialogListener {

    ActivityMainBinding binding;

    List<Recipe> recipesList;
    RecipesViewAdapter recipesAdapter;

    //If read external files permission denied, must avoid loading images from recipes
    private boolean noImage = false;

    private boolean transitioningActivity = false;

    private String lastSearchFilter;
    private boolean searchOpen = false;
    private boolean restoringState = false;

    private GetRecipesLoader recipesLoader;

    //Loader IDs
    private static final int LOADER_RECIPE_PREVIEWS = 101;

    //Permission request codes
    private static final int REQ_CODE_READ_EXTERNAL = 201;

    //Instance state IDs
    private static final String SEARCH_OPEN = "SEARCH_OPEN";
    private static final String SEARCH_FILTER = "SEARCH_FILTER";

    //TODO remove later
    private boolean inserting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.HomeTheme);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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

        if (savedInstanceState != null) {
            searchOpen = savedInstanceState.getBoolean(SEARCH_OPEN);
            lastSearchFilter = savedInstanceState.getString(SEARCH_FILTER);
        }

        if (!inserting)
            getSupportLoaderManager().initLoader(LOADER_RECIPE_PREVIEWS, null, loaderCallbacks);
        else {
            String path = Environment.getExternalStorageDirectory().getPath();
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", false, false, 1);
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", true, false, 2);
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", false, true, 3);
            Utility.InsertValue(this, path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg", true, true, 4);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(searchTextChangedListener);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Called when SearchView is collapsing
                if (searchItem.isActionViewExpanded()) {
                    AnimUtils.animateSearchToolbar(MainActivity.this, binding.tbarHome, 1, false, false);
                    binding.txtSearchNoItems.setVisibility(View.INVISIBLE);
                    searchOpen = false;
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                AnimUtils.animateSearchToolbar(MainActivity.this, binding.tbarHome, 1, true, true);
                searchOpen = true;
                return true;
            }
        });

        if (lastSearchFilter != null && !lastSearchFilter.isEmpty()) {
            restoringState = true;
            searchItem.expandActionView();
            searchView.setQuery(lastSearchFilter, false);
            searchView.clearFocus();
        }

        return true;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SEARCH_OPEN, searchOpen);
        outState.putString(SEARCH_FILTER, lastSearchFilter);
    }

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
                        noImage = true;
                        Utility.showPermissionDeniedSnackbar(binding.clyMainRoot);
                    }
                    UnblockLoader();
                }
                break;
        }
    }
    @Override
    public void onFeatureDisabled() {
        noImage = true;
        UnblockLoader();
    }

    /**
     * Allow the loader to continue loading recipes now that noImage state has been determined
     */
    private void UnblockLoader() {
        recipesLoader.onPermissionResponse();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void AddRecipe(View view) {
        Intent addRecipe = new Intent(getApplicationContext(), AddRecipeActivity.class);
        startActivity(addRecipe);
    }

    private LoaderManager.LoaderCallbacks<List<Recipe>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<Recipe>>() {
        @NonNull
        @Override
        public Loader<List<Recipe>> onCreateLoader(int id, @Nullable Bundle args) {
            Handler uiThread = new Handler(getMainLooper());
            return recipesLoader = new GetRecipesLoader(MainActivity.this, uiThread,
                    new GetRecipesLoader.ImagePermissionsListener() {
                        @Override
                        public void onImagePermRequested() {
                            int response = PermissionsHandler.AskForPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL);

                            if (response == PermissionsHandler.PERMISSION_GRANTED)
                                UnblockLoader();
                            else if (response == PermissionsHandler.PERMISSION_DENIED)
                                onFeatureDisabled();

                        }
                    });
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
            recipesAdapter.UpdateRecords(Collections.<Recipe>emptyList());
        }
    };

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipesViewAdapter
            extends RecyclerView.Adapter<RecipesViewAdapter.RecipeViewHolder> {

        List<Recipe> displayedRecipesList;

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecipesViewAdapter(List<Recipe> list) {
            if (list != null)
                displayedRecipesList = new ArrayList<>(list);
            else
                displayedRecipesList = new ArrayList<>();
        }

        @Override
        public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case COMPLEX:
                    return new ComplexViewHolder(RecipeCardComplexBinding.inflate(inflater, parent, false));
                case PHOTO_BASIC:
                    return new BasicPhotoViewHolder(RecipeCardPhotoBasicBinding.inflate(inflater, parent, false));
                case PHOTO_COMPLEX:
                    return new ComplexPhotoViewHolder(RecipeCardPhotoComplexBinding.inflate(inflater, parent, false));
                default:
                    return new BasicViewHolder(RecipeCardBasicBinding.inflate(inflater, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecipeViewHolder holder, int pos) {

            Recipe recipe = displayedRecipesList.get(pos);

            holder.bind(recipe);
        }

        @Override
        public int getItemViewType(int position) {

            Recipe item = displayedRecipesList.get(position);

            boolean isComplex = item.hasExtendedInfo();
            boolean hasPhoto = item.hasPhoto() && !noImage && Utility.FileExists(item.getImageFilePath());

            if (!hasPhoto)
                item.setImageFilePath(null);

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

        //Methods required for animated RecyclerView filtering
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

        class RecipeViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            ViewDataBinding binding;

            RecipeViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            public void bind(Recipe item) {
                binding.setVariable(BR.recipe, item);
                binding.executePendingBindings();
            }

            @Override
            public void onClick(View view) {
                ViewRecipe(displayedRecipesList.get(getAdapterPosition()), null);
            }

            @Override
            public boolean onLongClick(View view) {
                //TODO implement drag/drop initiation
                return true;
            }
        }

        class BasicViewHolder extends RecipeViewHolder {

            BasicViewHolder(RecipeCardBasicBinding itemBinding) {
                super(itemBinding.getRoot());
                binding = itemBinding;
            }
        }
        class ComplexViewHolder extends RecipeViewHolder {

            ComplexViewHolder(RecipeCardComplexBinding itemBinding) {
                super(itemBinding.getRoot());
                binding = itemBinding;
            }
        }
        class BasicPhotoViewHolder extends RecipeViewHolder {

            RecipeCardPhotoBasicBinding photoBinding;

            BasicPhotoViewHolder(RecipeCardPhotoBasicBinding itemBinding) {
                super(itemBinding.getRoot());
                binding = photoBinding = itemBinding;
            }

            @Override
            public void bind(Recipe item) {
                super.bind(item);

                //Set unique transition name for this specific
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
                ViewRecipe(displayedRecipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
            }
        }
        class ComplexPhotoViewHolder extends RecipeViewHolder {

            RecipeCardPhotoComplexBinding photoBinding;

            ComplexPhotoViewHolder(RecipeCardPhotoComplexBinding itemBinding) {
                super(itemBinding.getRoot());
                binding = photoBinding = itemBinding;
            }

            @Override
            public void bind(Recipe item) {
                super.bind(item);

                //Set unique transition name for this specific
                ViewCompat.setTransitionName(photoBinding.ivwCrdPreview,
                        getString(R.string.main_card_transition_name) + "_" +
                                item.getTitle() + "_" + Integer.toString(getAdapterPosition()));

                photoBinding.setVariable(BR.imageLoadedCallback, new RequestListener() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        Log.e(GLIDE, "Data Binding image loading failed (from filepath)", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                });
            }

            @Override
            public void onClick(View view) {
                ViewRecipe(displayedRecipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
            }
        }
    }

    private void ViewRecipe(Recipe recipe, ImageView sharedImageView) {

        //To ensure a card can only be clicked once
        if (transitioningActivity)
            return;

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
}