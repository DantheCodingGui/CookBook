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
    private RecyclerViewAdapter recipesAdapter;
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
        recipesAdapter = new RecyclerViewAdapter(recipesList);
        recipesView.setAdapter(recipesAdapter);
    }

    /**
     * Allows integration between the list of recipe objects and the recyclerview
     */
    class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHoldler> {

        private final int BASIC = 0, COMPLEX = 1;

        RecyclerViewAdapter(List<RecipeModel> list) {
            recipesList = list;
        }

        @Override
        public ViewHoldler onCreateViewHolder(ViewGroup parent, int viewType) {
            View layoutView;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case COMPLEX:
                    layoutView = inflater.inflate(R.layout.recipe_card_complex_layout, parent, false);
                    break;
                default:
                    layoutView = inflater.inflate(R.layout.recipe_card_layout, parent, false);
                    break;

            }
            return new ViewHoldler(layoutView);
        }

        @Override
        public void onBindViewHolder(ViewHoldler holder, int position) {
            //Set ViewHolder views to corresponding item's data in recipesList at position
            switch (holder.getItemViewType()) {
                case COMPLEX:

                    break;
                default:

                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (recipesList.get(position).hasPhoto())
                return COMPLEX;
            else
                return BASIC;
        }

        @Override
        public int getItemCount() {
            return recipesList.size();
        }

        /**
         * Hold individual list element view
         */
        class ViewHoldler extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            //View elements here you're showing

            ViewHoldler(View itemView) {
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

        /**
         * Hold individual list element view, includes photo of recipe
         */
        class ViewHoldlerPhoto extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            //View elements here you're showing

            ViewHoldlerPhoto(View itemView) {
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