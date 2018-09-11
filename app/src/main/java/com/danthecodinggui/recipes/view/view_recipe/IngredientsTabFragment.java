package com.danthecodinggui.recipes.view.view_recipe;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.databinding.FragmentIngredientsBinding;
import com.danthecodinggui.recipes.databinding.IngredientItemBinding;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.view.Loaders.GetIngredientsLoader;
import com.danthecodinggui.recipes.view.Loaders.UpdatingAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class IngredientsTabFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Ingredient>>,
        UpdatingAsyncTaskLoader.ProgressUpdateListener {

    FragmentIngredientsBinding binding;

    private static final int INGREDIENTS_LOADER = 111;

    private IngredientsViewAdapter ingredientsAdapter;
    private List<Ingredient> ingredientsList;

    private long recipeId;

    public IngredientsTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ingredients, container, false);
        View view = binding.getRoot();

        recipeId = getArguments().getLong(RECIPE_DETAIL_ID);

        getActivity().getSupportLoaderManager().initLoader(INGREDIENTS_LOADER, null, this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ingredientsAdapter = new IngredientsViewAdapter();
        ingredientsList = new ArrayList<>();
        binding.rvwIngredients.setAdapter(ingredientsAdapter);
        binding.rvwIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onStop() {
        super.onStop();

        //Loader will always reload data in onStart, so reset here
        ingredientsList.clear();
        ingredientsAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Ingredient>> loader) { }

    class IngredientsViewAdapter extends RecyclerView.Adapter<IngredientsViewAdapter.IngredientViewHolder> {

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new IngredientViewHolder(IngredientItemBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, int position) {
            Ingredient ingredient = ingredientsList.get(position);
            holder.bind(ingredient);
        }

        @Override
        public int getItemCount() {
            return ingredientsList.size();
        }

        class IngredientViewHolder extends RecyclerView.ViewHolder {

            IngredientItemBinding binding;

            IngredientViewHolder(IngredientItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.crdIngredientItem.setCardBackgroundColor(getResources().getColor(R.color.colorIngredients));
            }

            public void bind(Ingredient item) {
                binding.setIngredient(item);
                binding.executePendingBindings();
            }
        }
    }
}
