package com.danthecodinggui.recipes.view.view_recipe;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.view.Loaders.GetIngredientsLoader;
import com.danthecodinggui.recipes.view.Loaders.UpdatingAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class IngredientsTabFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Ingredient>>,
        UpdatingAsyncTaskLoader.ProgressUpdateListener {

    private static final int INGREDIENTS_LOADER = 111;

    @BindView(R.id.rvw_ingredients)
    RecyclerView ingredientsView;
    private IngredientsViewAdapter ingredientsAdapter;
    private List<Ingredient> ingredientsList;

    private long recipeId;

    public IngredientsTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingredients, container, false);

        ButterKnife.bind(this, view);

        recipeId = getArguments().getLong(RECIPE_DETAIL_ID);

        getActivity().getSupportLoaderManager().initLoader(INGREDIENTS_LOADER, null, this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ingredientsAdapter = new IngredientsViewAdapter();
        ingredientsList = new ArrayList<>();
        ingredientsView.setAdapter(ingredientsAdapter);
        ingredientsView.setLayoutManager(new LinearLayoutManager(getContext()));

//        ingredientsList.add("15 cherry tomatoes");
//        ingredientsList.add("1 tspn salt");
//        ingredientsList.add("1 tspn pepper");
//        ingredientsList.add("10 lettuce leaves");
//        ingredientsList.add("1 carrot");
//
//        ingredientsAdapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Loader<List<Ingredient>> onCreateLoader(int id, @Nullable Bundle args) {
        Handler uiThread = new Handler(Looper.getMainLooper());
        return new GetIngredientsLoader(getActivity(), uiThread, this, id, recipeId);
    }

    @Override
    public <T> void onProgressUpdate(int loaderId, T updateValue) {
        ingredientsList.addAll((List)updateValue);
        ingredientsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Ingredient>> loader, List<Ingredient> remainingIngredients) {
        ingredientsList.addAll(remainingIngredients);
        ingredientsAdapter.notifyDataSetChanged();

        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Ingredient>> loader) { }

    class IngredientsViewAdapter extends RecyclerView.Adapter<IngredientsViewAdapter.IngredientViewHolder> {

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new IngredientViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ingredient_item, parent, false));
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, int position) {
            holder.ingredient.setText(ingredientsList.get(position).getIngredientText());
        }

        @Override
        public int getItemCount() {
            return ingredientsList.size();
        }

        class IngredientViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.crd_ingredient_item)
            CardView background;

            @BindView(R.id.txt_ingredient_item)
            TextView ingredient;

            IngredientViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                background.setCardBackgroundColor(getResources().getColor(R.color.colorIngredients));
            }
        }
    }
}
