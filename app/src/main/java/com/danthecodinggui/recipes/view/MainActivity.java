package com.danthecodinggui.recipes.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.view.view_recipe.ViewRecipeActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get display data from data sourced here

        recipesView = findViewById(R.id.rvw_recipes);

        recipesList = new ArrayList<>();

        //Conditionally set RecyclerView layout manager depending on screen orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            recipesView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        else
            recipesView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //setup RecyclerView adapter
        recipesAdapter = new RecipesViewAdapter();
        recipesView.setAdapter(recipesAdapter);

        //Example cards TODO remove later
        recipesList.add(new RecipeModel("Sushi Sliders", 5, 6,
                10, 5, BitmapFactory.decodeResource(getResources(), R.drawable.sample_image)));
        recipesList.add(new RecipeModel("American Pancakes", 4, 7));
        recipesList.add(new RecipeModel("English Pancakes", 4, 7, 10, 3));
        recipesList.add(new RecipeModel("Spag Bol", 4, 7, BitmapFactory.decodeResource(getResources(), R.drawable.sample_image)));
        recipesAdapter.notifyDataSetChanged();
    }

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipesViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

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
            ((BasicViewHolder)holder).title.setText(recipesList.get(pos).getTitle());

            int ingredientsNo = recipesList.get(pos).getIngredientsNo();
            String ingredientsString = res.getQuantityString(R.plurals.txt_ingredients_no, ingredientsNo, ingredientsNo);
            ((BasicViewHolder)holder).ingredientsNo.setText(ingredientsString);

            int stepsNo = recipesList.get(pos).getStepsNo();
            String stepsString = res.getQuantityString(R.plurals.txt_method_steps_no, stepsNo, stepsNo);
            ((BasicViewHolder)holder).stepsNo.setText(stepsString);

            String kcalString;
            int kcals;

            //TODO deal with null returns from getter methods as any complex data is optional to the user
            switch (holder.getItemViewType()) {
                case COMPLEX:
                    kcals = recipesList.get(pos).getCalories();
                    kcalString = String.valueOf(res.getQuantityString(
                            R.plurals.txt_calories, kcals, kcals));
                    ((ComplexViewHoldler)holder).calories.setText(kcalString);
                    ((ComplexViewHoldler)holder).timeInMins.setText(String.valueOf(recipesList.get(pos).getTimeInMins()));
                    break;
                case PHOTO_BASIC:
                    ((BasicPhotoViewHolder)holder).preview.setImageBitmap(recipesList.get(pos).getPreview());
                    break;
                case PHOTO_COMPLEX:
                    kcals = recipesList.get(pos).getCalories();
                    kcalString = String.valueOf(res.getQuantityString(
                            R.plurals.txt_calories, kcals, kcals));
                    ((ComplexViewHoldler)holder).calories.setText(kcalString);

                    ((ComplexViewHoldler)holder).timeInMins.setText(String.valueOf(recipesList.get(pos).getTimeInMins()));
                    ((ComplexPhotoViewHolder)holder).preview.setImageBitmap(recipesList.get(pos).getPreview());
                    //TODO Use glide here for image loading
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            boolean isComplex = recipesList.get(position).hasFullRecipe();
            boolean hasPhoto = recipesList.get(position).hasPhoto();

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
            return recipesList.size();
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
        viewRecipe.putExtra("Title", recipeTitle);

        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO first check if card has image at all, if it does, get root view.image

            View imagePreview;

            //Recipe has an image associated with it
            if ((imagePreview = cardView.findViewById(R.id.ivw_crd_preview)) != null) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this, imagePreview, getString(R.string.transition_image_preview));

                viewRecipe.putExtra("Title", recipeTitle);
                ActivityCompat.startActivity(this, viewRecipe, options.toBundle());
            }
            else {
                //only transition from cardview not imageview
            }
        }
        else
            startActivity(viewRecipe);
    }
}