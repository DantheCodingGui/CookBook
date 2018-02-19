package com.danthecodinggui.recipes.view;

import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity {

    //RecyclerView components
    private RecyclerView recipesView;
    private RecyclerViewAdapter recipesAdapter;
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
        recipesAdapter = new RecyclerViewAdapter(recipesList);
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
    class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int BASIC = 0, COMPLEX = 1, PHOTO_BASIC = 2, PHOTO_COMPLEX = 3;

        RecyclerViewAdapter(List<RecipeModel> list) {
            recipesList = list;
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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            //TODO change so that each viewholder binds their own data (will have to also do super())
            ((BasicViewHolder)holder).title.setText(recipesList.get(position).getTitle());
            ((BasicViewHolder)holder).ingredientsNo.setText(String.valueOf(recipesList.get(position).getIngredientsNo()));
            ((BasicViewHolder)holder).stepsNo.setText(String.valueOf(recipesList.get(position).getStepsNo()));

            //TODO deal with null returns from getter methods as any complex data is optional to the user
            switch (holder.getItemViewType()) {
                case COMPLEX:
                    ((ComplexViewHoldler)holder).calories.setText(String.valueOf(recipesList.get(position).getCalories()));
                    ((ComplexViewHoldler)holder).timeInMins.setText(String.valueOf(recipesList.get(position).getTimeInMins()));
                    break;
                case PHOTO_BASIC:
                    ((BasicPhotoViewHolder)holder).preview.setImageBitmap(recipesList.get(position).getPreview());
                    break;
                case PHOTO_COMPLEX:
                    ((ComplexViewHoldler)holder).calories.setText(String.valueOf(recipesList.get(position).getCalories()));
                    ((ComplexViewHoldler)holder).timeInMins.setText(String.valueOf(recipesList.get(position).getTimeInMins()));
                    ((ComplexPhotoViewHolder)holder).preview.setImageBitmap(recipesList.get(position).getPreview());
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
}