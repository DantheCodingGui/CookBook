package com.danthecodinggui.recipes.view;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.Pair;
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
import android.widget.Filter;
import android.widget.Filterable;
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
import com.danthecodinggui.recipes.model.ProviderContract;
import com.danthecodinggui.recipes.model.object_models.Recipe;
import com.danthecodinggui.recipes.msc.AnimUtils;
import com.danthecodinggui.recipes.msc.IntentConstants;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.Utility;
import com.danthecodinggui.recipes.view.Loaders.GetRecipesLoader;
import com.danthecodinggui.recipes.view.Loaders.UpdatingAsyncTaskLoader;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.CARD_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_BUNDLE;
import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_OBJECT;
import static com.danthecodinggui.recipes.msc.LogTags.GLIDE;
import static com.danthecodinggui.recipes.msc.LogTags.PERMISSIONS;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Recipe>>,
        UpdatingAsyncTaskLoader.ProgressUpdateListener,
        GetRecipesLoader.ImagePermissionsListener,
        Utility.PermissionDialogListener {

    ActivityMainBinding binding;

    RecipesViewAdapter recipesAdapter;
    private List<Recipe> recipesList;

    //If read external files permission denied, must avoid loading images from recipes
    private boolean noImage = false;

    private GetRecipesLoader recipesLoader;

    //Loader IDs
    private static final int LOADER_RECIPE_PREVIEWS = 101;

    //Permission request codes
    private static final int REQ_CODE_READ_EXTERNAL = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        recipesList = new ArrayList<>();

        //Conditionally set RecyclerView layout manager depending on screen orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            binding.rvwRecipes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        else
            binding.rvwRecipes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //setup RecyclerView adapter
        recipesAdapter = new RecipesViewAdapter(recipesList);
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

        getSupportLoaderManager().initLoader(LOADER_RECIPE_PREVIEWS, null, this);


        //String path = Environment.getExternalStorageDirectory().getPath();
        //InsertValue(path + "/Download/pxqrocxwsjcc_2VgDbVfaysKmgiECiqcICI_Spaghetti-aglio-e-olio-1920x1080-thumbnail.jpg");
    }

    private void InsertValue(String imagePath) {

        ContentResolver resolver = getContentResolver();

        ContentValues values = new ContentValues();

        values.put(ProviderContract.RecipeEntry.VIEW_ORDER, 20);
        values.put(ProviderContract.RecipeEntry.TITLE, "Pasta Aglio E Olio");
        values.put(ProviderContract.RecipeEntry.CALORIES_PER_PERSON, 340);
        values.put(ProviderContract.RecipeEntry.DURATION, 20);
        values.put(ProviderContract.RecipeEntry.IMAGE_PATH, imagePath);

        Uri result = resolver.insert(
                ProviderContract.RECIPES_URI,
                values);

        long recipeId = ContentUris.parseId(result);

        //Ingredients
        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Spaghetti");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Garlic");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Parsley");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Olive Oil");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Red Pepper Flake");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.RecipeIngredientEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.RecipeIngredientEntry.INGREDIENT_NAME, "Chicken (Optional)");
        resolver.insert(
                ProviderContract.RECIPE_INGREDIENTS_URI,
                values);

        //Method

        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 1);
        values.put(ProviderContract.MethodStepEntry.TEXT, "Gradually heat up oil in pan and saute garlic until golden");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);

        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 2);
        values.put(ProviderContract.MethodStepEntry.TEXT, "Add Red Pepper Flake and chopped Parsley");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);


        values = new ContentValues();
        values.put(ProviderContract.MethodStepEntry.RECIPE_ID, recipeId);
        values.put(ProviderContract.MethodStepEntry.STEP_NO, 3);
        values.put(ProviderContract.MethodStepEntry.TEXT, "Toss with cooked spaghetti and add cooked chicken if desired");
        resolver.insert(
                ProviderContract.METHOD_URI,
                values);
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
                recipesAdapter.getFilter().filter(newText);
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
    public void onImagePermRequested() {
        int response = PermissionsHandler.AskForPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE, REQ_CODE_READ_EXTERNAL, false);

        switch(response) {
            case PermissionsHandler.PERMISSION_ALREADY_GRANTED:
                Log.v(PERMISSIONS, "Storage permission already granted");
                break;
            case PermissionsHandler.PERMISSION_PREVIOUSLY_DENIED:
                Log.v(PERMISSIONS, "Storage permission denied, app won't load images");
                noImage = true;
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQ_CODE_READ_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                    Utility.showPermissionDeniedDialog(this,
                    R.string.perm_dialog_read_external,
                    binding.clyMainRoot,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                            REQ_CODE_READ_EXTERNAL,
                    this);
                    break;
        }
    }
    @Override
    public void onFeatureDisabled() {
        noImage = true;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, Pair.create(view, getString(R.string.add_transition)));
            int revealX = (int) (view.getX() + view.getWidth() / 2);
            int revealY = (int) (view.getY() + view.getHeight() / 2);

            addRecipe.putExtra(IntentConstants.EXTRA_CIRCULAR_REVEAL_X, revealX);
            addRecipe.putExtra(IntentConstants.EXTRA_CIRCULAR_REVEAL_Y, revealY);
            ActivityCompat.startActivity(MainActivity.this, addRecipe, options.toBundle());
            return;
        }
        startActivity(addRecipe);
    }

    @Override
    public android.support.v4.content.Loader<List<Recipe>> onCreateLoader(int id, Bundle args) {
        Handler uiThread = new Handler(getMainLooper());
        return recipesLoader = new GetRecipesLoader(this, uiThread, this,
                this, LOADER_RECIPE_PREVIEWS);
    }

    @Override
    public <T> void onProgressUpdate(int loaderId, T updateValue) {
        switch(loaderId) {
            case LOADER_RECIPE_PREVIEWS:
                UpdateRecipesList((List)updateValue);
                break;
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<List<Recipe>> loader, List<Recipe> remainingRecords) {
        //Add the remaining records (not passed through onProgressUpdate) to recipeList
        recipesList.addAll(remainingRecords);
        recipesAdapter.notifyDataSetChanged();

        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onStop() {
        super.onStop();

        //Loader will always reload data in onStart, so reset here
        recipesList.clear();
        recipesAdapter.notifyDataSetChanged();
    }

    private void UpdateRecipesList(List<Recipe> newRecords) {
        recipesList.addAll(newRecords);
        recipesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<List<Recipe>> loader) {}

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipesViewAdapter
            extends RecyclerView.Adapter<RecipesViewAdapter.RecipeViewHolder> implements Filterable {

        List<Recipe> filteredRecipesList;

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecipesViewAdapter(List<Recipe> list) {
            filteredRecipesList = list;
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

            Recipe recipe = filteredRecipesList.get(pos);

            holder.bind(recipe);
        }

        @Override
        public int getItemViewType(int position) {
            boolean isComplex = filteredRecipesList.get(position).hasExtendedInfo();
            boolean hasPhoto = filteredRecipesList.get(position).hasPhoto() && !noImage;

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
            return filteredRecipesList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        filteredRecipesList = recipesList;
                    }
                    else {
                        List<Recipe> filteredList = new ArrayList<>();
                        for (Recipe row : recipesList) {

                            if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }

                        filteredRecipesList = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredRecipesList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                     filteredRecipesList = (ArrayList<Recipe>) filterResults.values;

                    if (filteredRecipesList.size() == 0)
                        binding.txtSearchNoItems.setVisibility(View.VISIBLE);
                    else
                        binding.txtSearchNoItems.setVisibility(View.INVISIBLE);

                    recipesAdapter.notifyDataSetChanged();
                }
            };
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
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        startPostponedEnterTransition();
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

                photoBinding.setImageLoadedCallback(new RequestListener() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        Log.e(GLIDE, "Data Binding image loading failed (from filepath)", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                        startPostponedEnterTransition();
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

        Intent viewRecipe = new Intent(this, ViewRecipeActivity.class);
        ActivityOptions options;

        Bundle recipeBundle = new Bundle();
        recipeBundle.putParcelable(RECIPE_DETAIL_OBJECT, recipe);

        viewRecipe.putExtra(RECIPE_DETAIL_BUNDLE, recipeBundle);

        if (Utility.atLeastLollipop()) {

            if (sharedImageView != null) {

                viewRecipe.putExtra(CARD_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));

                options = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        sharedImageView,
                        ViewCompat.getTransitionName(sharedImageView));
                startActivity(viewRecipe, options.toBundle());
                return;

            }
        }
        //Should just start activity normally
        startActivity(viewRecipe);
    }
}