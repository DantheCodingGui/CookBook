package com.danthecodinggui.recipes;

//Also need data model with get/set methods,
//list item layout (i.e. CardView?) and Gradle

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Display all stored recipes
 */
public class MainActivity extends AppCompatActivity {

    //RecyclerView components
    private RecyclerView recipesView;
    private RecipeAdapter recipesAdapter;
    private List<RecipeModel> recipesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get display data from data sourced here

        recipesView = (RecyclerView) findViewById(R.id.rvw_recipes);

        //conditionally set RecyclerView layout manager depending on screen orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            recipesView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));//INSERT LANDSCAPE LAYOUT MANAGER
        else
            recipesView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));//INSERT PORTRAIT LAYOUT MANAGER

        //setup RecyclerView adapter
        recipesAdapter = new RecipeAdapter(recipesList);
        recipesView.setAdapter(recipesAdapter);
    }

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecipeAdapter
            extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

        RecipeAdapter(List<RecipeModel> list) {
            recipesList = list;
        }

        @Override
        public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_card_layout, parent, false);
            return new RecipeViewHolder(layoutView);
        }

        @Override
        public void onBindViewHolder(RecipeViewHolder holder, int position) {
            //Set ViewHolder views to corresponding item's data in recipesList at position

        }

        @Override
        public int getItemCount() {
            return recipesList.size();
        }

        /**
         * Hold individual list element view
         */
        class RecipeViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            //View elements here you're showing

            RecipeViewHolder(View itemView) {
                super(itemView);
                //Initialise views here

                //Optionally setup click listeners
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            //OPTIONAL
            @Override
            public void onClick(View view) {
                //Click the list element
            }

            //OPTIONAL
            @Override
            public boolean onLongClick(View view) {
                //Long click the list element

                //Return has method handled the click
                return true;
            }
        }
    }
}