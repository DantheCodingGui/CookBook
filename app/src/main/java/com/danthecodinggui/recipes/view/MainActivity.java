package com.danthecodinggui.recipes.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.IntentConstants;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity {

    //RecyclerView components
    private RecyclerView recipesView;
    private RecipesViewAdapter recipesAdapter;
    private List<RecipeModel> recipesList;
    private FloatingActionButton addRecipe;

    private ConstraintLayout searchNoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get display data from data sourced here

        recipesView = findViewById(R.id.rvw_recipes);
        addRecipe = findViewById(R.id.fab_add_recipe);

        searchNoItems = findViewById(R.id.csly_search_not_found);

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

        Toolbar toolbar = findViewById(R.id.tbar_home);
        setSupportActionBar(toolbar);

        //Example cards TODO remove later
        recipesList.add(new RecipeModel("American Pancakes", 4, 7));
        recipesList.add(new RecipeModel("Sushi Sliders", 5, 6,
                10, 5, BitmapFactory.decodeResource(getResources(), R.drawable.sample_image)));
        recipesList.add(new RecipeModel("English Pancakes", 4, 7, 10, 3));
        recipesList.add(new RecipeModel("Spag Bol", 4, 7, BitmapFactory.decodeResource(getResources(), R.drawable.sample_image)));
        recipesAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
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

        initSearchView(menu);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow)
    {
        final View myView = findViewById(viewID);

        int width=myView.getWidth();

        if(posFromRight>0)
            width-=(posFromRight*getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material))-(getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)/ 2);

        if(containsOverflow)
            width-=getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx=width;
        int cy=myView.getHeight()/2;

        Animator anim;
        if(isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0,(float)width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float)width, 0);

        anim.setDuration((long)220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isShow)
                {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if(isShow)
            myView.setVisibility(View.VISIBLE);

        // start the animation
        anim.start();
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

    public void initSearchView(Menu menu)
    {
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();

        // Set hint and the text colors

        EditText txtSearch = (searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Search..");
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setTextColor(getResources().getColor(R.color.colorPrimary));

        // Set the cursor

        AutoCompleteTextView searchTextView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleReveal(R.id.tbar_home, 1, false, true);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                circleReveal(R.id.tbar_home, 1, false, false);
                return true;
            }
        });
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

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipesViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

        List<RecipeModel> filteredRecipesList;

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecipesViewAdapter(List<RecipeModel> list) {
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
            boolean isComplex = filteredRecipesList.get(position).hasFullRecipe();
            boolean hasPhoto = filteredRecipesList.get(position).hasPhoto();

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
                        List<RecipeModel> filteredList = new ArrayList<>();
                        for (RecipeModel row : recipesList) {

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
                     filteredRecipesList = (ArrayList<RecipeModel>) filterResults.values;

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

            TextView title;
            TextView ingredientsNo;
            TextView stepsNo;

            BasicViewHolder(View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.txt_crd_title);
                ingredientsNo = itemView.findViewById(R.id.txt_crd_ingredient_no);
                stepsNo = itemView.findViewById(R.id.txt_crd_steps_no);

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

            TextView calories;
            TextView timeInMins;

            ComplexViewHoldler(View itemView) {
                super(itemView);

                calories = itemView.findViewById(R.id.txt_crd_cal);
                timeInMins = itemView.findViewById(R.id.txt_crd_duration);
            }
        }

        /**
         * Adds an image preview of the completed dish
         */
        class BasicPhotoViewHolder extends BasicViewHolder {

            ImageView preview;

            BasicPhotoViewHolder(View itemView) {
                super(itemView);

                preview = itemView.findViewById(R.id.ivw_crd_preview);
            }
        }

        /**
         * Adds an image preview of the completed dish
         */
        class ComplexPhotoViewHolder extends ComplexViewHoldler {

            ImageView preview;

            ComplexPhotoViewHolder(View itemView) {
                super(itemView);

                preview = itemView.findViewById(R.id.ivw_crd_preview);
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