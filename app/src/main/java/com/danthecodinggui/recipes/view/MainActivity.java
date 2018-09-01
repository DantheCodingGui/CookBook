package com.danthecodinggui.recipes.view;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.Toast;

import com.danthecodinggui.recipes.BR;
import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.ActivityMainBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardBasicBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardComplexBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardPhotoBasicBinding;
import com.danthecodinggui.recipes.databinding.RecipeCardPhotoComplexBinding;
import com.danthecodinggui.recipes.model.RecipeViewModel;
import com.danthecodinggui.recipes.msc.AnimUtils;
import com.danthecodinggui.recipes.msc.IntentConstants;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.msc.Utility;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.CARD_TRANSITION_NAME;
import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DB_ID;
import static com.danthecodinggui.recipes.msc.LogTags.PERMISSIONS;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<RecipeViewModel>>,
        UpdatingAsyncTaskLoader.ProgressUpdateListener,
        GetRecipesLoader.ImagePermissionsListener {

    ActivityMainBinding binding;

    RecipesViewAdapter recipesAdapter;
    private List<RecipeViewModel> recipesList;

    //If read external files permission denied, must avoid loading images from recipes
    private boolean noImage = false;

    private GetRecipesLoader recipesLoader;

    //Loader IDs
    private static final int PREVIEWS_TOKEN = 101;

    //Permission request codes
    private static final int REQUEST_READ_EXTERNAL = 201;

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

        getSupportLoaderManager().initLoader(PREVIEWS_TOKEN, null, this);



        String path = Environment.getExternalStorageDirectory().getPath();
        String photoPath = path + "/Download/food-dinner-lunch-70497.jpg";

        //Example cards TODO remove later
        recipesList.add(new RecipeViewModel("American Pancakes"));
        recipesList.add(new RecipeViewModel("Lasagna", photoPath));
        recipesList.add(new RecipeViewModel("English Pancakes", 10, 300));
        recipesList.add(new RecipeViewModel("Spag Bol", 4, 550, photoPath));
        recipesAdapter.notifyDataSetChanged();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_READ_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission now granted!", Toast.LENGTH_SHORT).show();
                    recipesLoader.onPermissionResponse(true);
                }
                else {
                    //Alert the user why this permission is needed
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.perm_dialog_read_external)
                            .setNegativeButton(R.string.perm_dialog_butt_deny, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    recipesLoader.onPermissionResponse(false);
                                    noImage = true;

                                    //Alert the user how they can re-enable the feature
                                    Snackbar.make(binding.clyMainRoot,
                                                    R.string.perm_snackbar_msg,
                                                    Snackbar.LENGTH_LONG
                                            )
                                            .show();
                                }
                            })
                            .setPositiveButton(R.string.perm_dialog_butt_permit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    PermissionsHandler.AskForPermission(MainActivity.this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL, true);
                                }
                            })
                            .create()
                            .show();
                }
                    break;
        }

        getSupportLoaderManager().initLoader(PREVIEWS_TOKEN, null, this);
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
    public android.support.v4.content.Loader<List<RecipeViewModel>> onCreateLoader(int id, Bundle args) {
        Handler uiThread = new Handler(getMainLooper());
        return recipesLoader = new GetRecipesLoader(this, uiThread, this,
                this, PREVIEWS_TOKEN);
    }

    @Override
    public <T> void onProgressUpdate(int loaderId, T updateValue) {
        switch(loaderId) {
            case PREVIEWS_TOKEN:
                UpdateRecipesList((List)updateValue);
                break;
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<List<RecipeViewModel>> loader, List<RecipeViewModel> remainingRecords) {
        //Add the remaining records (not passed through onProgressUpdate) to recipeList
        recipesList.addAll(remainingRecords);
    }

    private void UpdateRecipesList(List<RecipeViewModel> newRecords) {
        recipesList.addAll(newRecords);
        recipesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onImagePermRequested() {
        int response = PermissionsHandler.AskForPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL, false);

        //If permission status already decided, alert loader immediately
        switch(response) {
            case PermissionsHandler.PERMISSION_ALREADY_GRANTED:
                Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show();

                recipesLoader.onPermissionResponse(true);
                break;
            case PermissionsHandler.PERMISSION_PREVIOUSLY_DENIED:
                Log.v(PERMISSIONS, "Storage permission denied, app won't load images");
                Toast.makeText(this, "Permission previously denied!", Toast.LENGTH_SHORT).show();

                recipesLoader.onPermissionResponse(false);
                noImage = true;
                break;
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<List<RecipeViewModel>> loader) {}

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipesViewAdapter
            extends RecyclerView.Adapter<RecipesViewAdapter.RecipeViewHolder> implements Filterable {

        List<RecipeViewModel> filteredRecipesList;

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecipesViewAdapter(List<RecipeViewModel> list) {
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

            RecipeViewModel recipe = filteredRecipesList.get(pos);

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
                        List<RecipeViewModel> filteredList = new ArrayList<>();
                        for (RecipeViewModel row : recipesList) {

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
                     filteredRecipesList = (ArrayList<RecipeViewModel>) filterResults.values;

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

            public void bind(RecipeViewModel item) {
                binding.setVariable(BR.recipe, item);
                binding.executePendingBindings();
            }

            @Override
            public void onClick(View view) {
                //TODO implement transition to view activity
                //TODO add flag to call stating photo/no photo to choose layout to inflate
                //TODO make simpler viewrecipe layout without collapsingtoolbarlayout
                ViewRecipe(getAdapterPosition(), null);
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
            public void bind(RecipeViewModel item) {
                super.bind(item);

                //Set unique transition name for this specific
                ViewCompat.setTransitionName(photoBinding.ivwCrdPreview,
                        getString(R.string.main_card_transition_name) + "_" +
                                item.getTitle() + "_" + Integer.toString(getAdapterPosition()));
            }

            @Override
            public void onClick(View view) {
                ViewRecipe(getAdapterPosition(), photoBinding.ivwCrdPreview);
            }
        }
        class ComplexPhotoViewHolder extends RecipeViewHolder {

            RecipeCardPhotoComplexBinding photoBinding;

            ComplexPhotoViewHolder(RecipeCardPhotoComplexBinding itemBinding) {
                super(itemBinding.getRoot());
                binding = photoBinding = itemBinding;
            }

            @Override
            public void bind(RecipeViewModel item) {
                super.bind(item);

                //Set unique transition name for this specific
                ViewCompat.setTransitionName(photoBinding.ivwCrdPreview,
                        getString(R.string.main_card_transition_name) + "_" +
                                item.getTitle() + "_" + Integer.toString(getAdapterPosition()));
            }

            @Override
            public void onClick(View view) {
                ViewRecipe(getAdapterPosition(), photoBinding.ivwCrdPreview);
            }
        }
    }

    private void ViewRecipe(int recipeId, ImageView sharedImageView) {

        Intent viewRecipe = new Intent(this, ViewRecipeActivity.class);
        ActivityOptions options;
        viewRecipe.putExtra(RECIPE_DB_ID, recipeId);
        //Todo remove line later when records loaded from db
        viewRecipe.putExtra("hasPhoto", sharedImageView != null);

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