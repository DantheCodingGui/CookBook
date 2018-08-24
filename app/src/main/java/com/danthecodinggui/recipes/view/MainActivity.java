package com.danthecodinggui.recipes.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.AnimationUtils;
import com.danthecodinggui.recipes.msc.IntentConstants;
import com.danthecodinggui.recipes.msc.PermissionsHandler;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.danthecodinggui.recipes.msc.LogTags.PERMISSIONS;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<RecipeViewModel>>,
        UpdatingAsyncTaskLoader.ProgressUpdateListener,
        GetRecipesLoader.ImagePermissionsListener {

    //RecyclerView components
    @BindView(R.id.rvw_recipes) RecyclerView recipesView;
    RecipesViewAdapter recipesAdapter;
    private List<RecipeViewModel> recipesList;
    @BindView(R.id.fab_add_recipe) FloatingActionButton addRecipe;

    @BindView(R.id.txt_search_no_items) TextView searchNoItems;
    @BindView(R.id.txt_no_items) TextView noItems;

    @BindView(R.id.tbar_home) Toolbar homeBar;

    //If read external files permission denied, must avoid loading images from recipes
    private boolean noImage = false;

    GetRecipesLoader recipesLoader;

    //Loader IDs
    private static final int PREVIEWS_TOKEN = 101;

    //Permission request codes
    private static final int REQUEST_READ_EXTERNAL = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //Get display data from data sourced here

        //Link Butterknife instance to Activity
        ButterKnife.bind(this);

        recipesList = new ArrayList<>();

        //Conditionally set RecyclerView layout manager depending on screen orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            recipesView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        else
            recipesView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //setup RecyclerView adapter
        recipesAdapter = new RecipesViewAdapter(recipesList);
        recipesView.setAdapter(recipesAdapter);

        //Show/hide floating action button on recyclerview scroll
        recipesView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && addRecipe.getVisibility() == View.VISIBLE) {
                    addRecipe.hide();
                } else if (dy < 0 && addRecipe.getVisibility() != View.VISIBLE) {
                    addRecipe.show();
                }
            }
        });

        setSupportActionBar(homeBar);

        getSupportLoaderManager().initLoader(PREVIEWS_TOKEN, null, this);

        /*
        //Example cards TODO remove later
        recipesList.add(new RecipeViewModel("American Pancakes", false));
        recipesList.add(new RecipeViewModel("Sushi Sliders",
                10, 5,true));
        recipesList.add(new RecipeViewModel("English Pancakes", 10, 3, true));
        recipesList.add(new RecipeViewModel("Spag Bol", 4, 7, true));
        recipesAdapter.notifyDataSetChanged();#
        */

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
                    AnimationUtils.animateSearchToolbar(MainActivity.this, homeBar, 1, false, false);
                    searchNoItems.setVisibility(View.INVISIBLE);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                AnimationUtils.animateSearchToolbar(MainActivity.this, homeBar, 1, true, true);
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
                                    Snackbar.make(findViewById(R.id.cly_main_root),
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
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

        List<RecipeViewModel> filteredRecipesList;

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecipesViewAdapter(List<RecipeViewModel> list) {
            filteredRecipesList = list;
        }

        @Override
        public BasicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            //TODO change so each viewholder builds thenselves (probs a static method)
            switch (viewType) {
                case COMPLEX:
                    return new ComplexViewHoldler(inflater.inflate(R.layout.recipe_card_complex, parent, false));
                case PHOTO_BASIC:
                    return new BasicPhotoViewHolder(inflater.inflate(R.layout.recipe_card_photo_basic, parent, false));
                case PHOTO_COMPLEX:
                    return new ComplexPhotoViewHolder(inflater.inflate(R.layout.recipe_card_photo_complex, parent, false));
                default:
                    return new BasicViewHolder(inflater.inflate(R.layout.recipe_card_basic, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {

            Resources res = getResources();

            //TODO change so that each viewholder binds their own data (will have to also do super())
            ((BasicViewHolder)holder).title.setText(filteredRecipesList.get(pos).getTitle());

            int ingredientsNo = filteredRecipesList.get(pos).getIngredientsNo();
            String ingredientsString = res.getQuantityString(R.plurals.txt_ingredients_no, ingredientsNo, ingredientsNo);
            ((BasicViewHolder)holder).ingredientsNo.setText(ingredientsString);

            int stepsNo = filteredRecipesList.get(pos).getStepsNo();
            String stepsString = res.getQuantityString(R.plurals.txt_method_steps_no, stepsNo, stepsNo);
            ((BasicViewHolder)holder).stepsNo.setText(stepsString);

            //ViewCompat.setTransitionName(holder.itemView,
             //       getString(R.string.main_card_transition_name) + "_" + Integer.toString(pos));

            String kcalString;
            int kcals;

            //TODO deal with null returns from getter methods as any complex data is optional to the user
            switch (holder.getItemViewType()) {
                case COMPLEX:
                    kcals = filteredRecipesList.get(pos).getCalories();
                    kcalString = String.valueOf(res.getQuantityString(
                            R.plurals.txt_calories, kcals, kcals));
                    ((ComplexViewHoldler)holder).calories.setText(kcalString);
                    ((ComplexViewHoldler)holder).timeInMins.setText(String.valueOf(filteredRecipesList.get(pos).getTimeInMins()));
                    break;
                case PHOTO_BASIC:
                    ((BasicPhotoViewHolder)holder).preview.setImageBitmap(filteredRecipesList.get(pos).getPreview());
                    break;
                case PHOTO_COMPLEX:
                    kcals = filteredRecipesList.get(pos).getCalories();
                    kcalString = String.valueOf(res.getQuantityString(
                            R.plurals.txt_calories, kcals, kcals));
                    ((ComplexViewHoldler)holder).calories.setText(kcalString);

                    ((ComplexViewHoldler)holder).timeInMins.setText(String.valueOf(filteredRecipesList.get(pos).getTimeInMins()));
                    ((ComplexPhotoViewHolder)holder).preview.setImageBitmap(filteredRecipesList.get(pos).getPreview());
                    //TODO Use glide here for image loading
                    break;
            }
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
                        searchNoItems.setVisibility(View.VISIBLE);
                    else
                        searchNoItems.setVisibility(View.INVISIBLE);

                    recipesAdapter.notifyDataSetChanged();
                }
            };
        }

        /**
         * Parent class with values all cards possess
         */
        class BasicViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            @BindView(R.id.txt_crd_title) TextView title;
            @BindView(R.id.txt_crd_ingredient_no) TextView ingredientsNo;
            @BindView(R.id.txt_crd_steps_no) TextView stepsNo;

            BasicViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);

                //Optionally setup click listeners
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                //TODO implement transition to view activity
                //TODO add flag to call stating photo/no photo to choose layout to inflate
                //TODO make simpler viewrecipe layout without collapsingtoolbarlayout
                ViewRecipe(view, title.getText().toString());
            }

            @Override
            public boolean onLongClick(View view) {
                //TODO implement drag/drop initiation
                return true;
            }
        }

        /**
         * Adds calorie and/or time to make information
         */
        class ComplexViewHoldler extends BasicViewHolder {

            @BindView(R.id.txt_crd_cal) TextView calories;
            @BindView(R.id.txt_crd_duration) TextView timeInMins;

            ComplexViewHoldler(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
            }
        }

        /**
         * Adds an image preview of the completed dish
         */
        class BasicPhotoViewHolder extends BasicViewHolder {

            @BindView(R.id.ivw_crd_preview) ImageView preview;

            BasicPhotoViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
            }
        }

        /**
         * Adds an image preview of the completed dish
         */
        class ComplexPhotoViewHolder extends ComplexViewHoldler {

            @BindView(R.id.ivw_crd_preview) ImageView preview;

            ComplexPhotoViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
            }
        }
    }

    private void ViewRecipe(View cardView, String recipeTitle) {

        Intent viewRecipe = new Intent(getApplicationContext(), ViewRecipeActivity.class);
        ActivityOptions options = null;
        viewRecipe.putExtra("Title", recipeTitle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO get shared transitions working

            String transitionName = ViewCompat.getTransitionName(cardView);
            //viewRecipe.putExtra(CARD_TRANSITION_NAME, transitionName);
            options = ActivityOptions.makeSceneTransitionAnimation(this, cardView, transitionName);
            ActivityCompat.startActivity(this, viewRecipe, options.toBundle());
            return;

            /*
            View imagePreview;

            //Recipe has an image associated with it
            if ((imagePreview = cardView.findViewById(R.id.ivw_crd_preview)) != null) {
                options = ActivityOptions.makeSceneTransitionAnimation(
                        this, imagePreview, getString(R.string.transition_image_preview));
                ActivityCompat.startActivity(this, viewRecipe, options.toBundle());
                return;
            }
            */
        }
        //Should just start activity normally
        startActivity(viewRecipe);
    }
}