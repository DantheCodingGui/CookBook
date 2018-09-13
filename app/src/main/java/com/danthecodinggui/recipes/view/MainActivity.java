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
import android.support.v4.app.ActivityCompat;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.danthecodinggui.recipes.msc.IntentConstants;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.Utility;
import com.danthecodinggui.recipes.view.Loaders.GetRecipesLoader;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.IMAGE_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_BUNDLE;
import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_OBJECT;
import static com.danthecodinggui.recipes.msc.LogTags.DATA_LOADING;
import static com.danthecodinggui.recipes.msc.LogTags.GLIDE;
import static com.danthecodinggui.recipes.msc.LogTags.PERMISSIONS;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity
        implements Utility.PermissionDialogListener {

    ActivityMainBinding binding;

    RecipesViewAdapter recipesAdapter;

    //If read external files permission denied, must avoid loading images from recipes
    private boolean noImage = false;

    private boolean transitioningActivity = false;

    private GetRecipesLoader recipesLoader;

    //Loader IDs
    private static final int LOADER_RECIPE_PREVIEWS = 101;

    //Permission request codes
    private static final int REQ_CODE_READ_EXTERNAL = 201;

    //TODO remove later
    private boolean inserting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.HomeTheme);
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //Conditionally set RecyclerView layout manager depending on screen orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            binding.rvwRecipes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        else
            binding.rvwRecipes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //setup RecyclerView adapter
        recipesAdapter = new RecipesViewAdapter(null);
        binding.rvwRecipes.setAdapter(recipesAdapter);

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
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recipesAdapter.filter(newText);
                return false;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Called when SearchView is collapsing
                if (searchItem.isActionViewExpanded()) {
                    AnimUtils.animateSearchToolbar(MainActivity.this, binding.tbarHome, 1, false, false);
                    binding.txtSearchNoItems.setVisibility(View.INVISIBLE);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                AnimUtils.animateSearchToolbar(MainActivity.this, binding.tbarHome, 1, true, true);
                return true;
            }
        });

        return true;
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
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        UnblockLoader();
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                    Utility.showPermissionDeniedDialog(this,
                            R.string.perm_dialog_read_external,
                            binding.clyMainRoot,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            REQ_CODE_READ_EXTERNAL,
                            this);
                }
                break;
        }
    }
    @Override
    public void onFeatureDisabled() {
        noImage = true;
        UnblockLoader();
    }
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

        if (Utility.atLeastLollipop()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, Pair.create(view, getString(R.string.add_transition)));
            int revealX = (int) (view.getX() + view.getWidth() / 2);
            int revealY = (int) (view.getY() + view.getHeight() / 2);

            addRecipe.putExtra(IntentConstants.EXTRA_CIRCULAR_REVEAL_X, revealX);
            addRecipe.putExtra(IntentConstants.EXTRA_CIRCULAR_REVEAL_Y, revealY);
            ActivityCompat.startActivity(MainActivity.this, addRecipe, options.toBundle());
            return;
        }
        startActivity(addRecipe);
    }

    private LoaderManager.LoaderCallbacks<List<Recipe>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<Recipe>>() {
        //@SuppressWarnings("unchecked")
        @NonNull
        @Override
        public Loader<List<Recipe>> onCreateLoader(int id, @Nullable Bundle args) {
            Handler uiThread = new Handler(getMainLooper());
            return recipesLoader = new GetRecipesLoader(MainActivity.this, uiThread,
                    new GetRecipesLoader.ImagePermissionsListener() {
                        @Override
                        public void onImagePermRequested() {
                            int response = PermissionsHandler.AskForPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL, false);

                            switch(response) {
                                case PermissionsHandler.PERMISSION_ALREADY_GRANTED:
                                    Log.v(PERMISSIONS, "Storage permission already granted");
                                    UnblockLoader();
                                    break;
                                case PermissionsHandler.PERMISSION_PREVIOUSLY_DENIED:
                                    Log.v(PERMISSIONS, "Storage permission denied, app won't load images");
                                    noImage = true;
                                    UnblockLoader();
                                    break;
                            }
                        }
                    });
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<Recipe>> loader, List<Recipe> loadedRecipes) {
            if (loadedRecipes.size() == 0)
                binding.txtNoItems.setVisibility(View.VISIBLE);
            else
                binding.txtNoItems.setVisibility(View.INVISIBLE);

            recipesAdapter.UpdateRecords(loadedRecipes);

            Log.v(DATA_LOADING, "Load complete");

            //TODO: loaders broken, need to get it to work always BOTH with/without dontkeepactivities
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

        List<Recipe> unfilteredRecipesList;
        List<Recipe> recipesList;

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecipesViewAdapter(List<Recipe> list) {
            recipesList = list;
            unfilteredRecipesList = new ArrayList<>();
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

            Recipe recipe = recipesList.get(pos);

            holder.bind(recipe);
        }

        @Override
        public int getItemViewType(int position) {
            boolean isComplex = recipesList.get(position).hasExtendedInfo();
            boolean hasPhoto = recipesList.get(position).hasPhoto() && !noImage;

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
            if (recipesList == null)
                return 0;
            return recipesList.size();
        }

        void UpdateRecords(List<Recipe> updatedRecords) {
            if (updatedRecords != null) {
                if (unfilteredRecipesList == null || unfilteredRecipesList.isEmpty()) {
                    unfilteredRecipesList = recipesList = updatedRecords;
                    notifyItemRangeInserted(0, updatedRecords.size());
                }
                else {
                    unfilteredRecipesList = recipesList = updatedRecords;
                    notifyItemRangeChanged(0, updatedRecords.size());
                }
            }
        }

        void filter(String searchText) {
            recipesList.clear();
            if (searchText.isEmpty())
                recipesList.addAll(unfilteredRecipesList);
            else {
                searchText = searchText.toLowerCase();
                for (Recipe r : unfilteredRecipesList) {
                    if (r.getTitle().toLowerCase().contains(searchText))
                        recipesList.add(r);
                }
            }
            if (recipesList.size() == 0)
                binding.txtSearchNoItems.setVisibility(View.VISIBLE);
            else
                binding.txtSearchNoItems.setVisibility(View.INVISIBLE);

            notifyDataSetChanged();
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
                ViewRecipe(recipesList.get(getAdapterPosition()), null);
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
                ViewRecipe(recipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
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
                ViewRecipe(recipesList.get(getAdapterPosition()), photoBinding.ivwCrdPreview);
            }
        }
    }

    private void ViewRecipe(Recipe recipe, ImageView sharedImageView) {

        //To ensure a card can only be clicked once
        if (transitioningActivity)
            return;

        Intent viewRecipe = new Intent(this, ViewRecipeActivity.class);
        ActivityOptions options;

        Bundle recipeBundle = new Bundle();
        recipeBundle.putParcelable(RECIPE_DETAIL_OBJECT, recipe);

        viewRecipe.putExtra(RECIPE_DETAIL_BUNDLE, recipeBundle);

        transitioningActivity = true;

        if (Utility.atLeastLollipop()) {

            if (sharedImageView != null) {

                //To have both the image and navbar as shared elements, must both pass Pairs and manually
                //set transition name for image
                viewRecipe.putExtra(IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));

                Pair<View, String> image = Pair.create((View)sharedImageView, ViewCompat.getTransitionName(sharedImageView));
                Pair<View, String> navbar = Pair.create(
                        findViewById(android.R.id.navigationBarBackground),
                        Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);

                Pair[] transitions = {image, navbar};

                options = ActivityOptions.makeSceneTransitionAnimation(this, transitions);
                startActivity(viewRecipe, options.toBundle());
                return;

            }
        }
        //Should just start activity normally
        startActivity(viewRecipe);
    }
}