package com.danthecodinggui.recipes.view.view_recipe;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import com.danthecodinggui.recipes.databinding.FragmentMethodBinding;
import com.danthecodinggui.recipes.databinding.ViewMethodItemBinding;
import com.danthecodinggui.recipes.model.object_models.MethodStep;
import com.danthecodinggui.recipes.view.Loaders.GetMethodStepsLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static com.danthecodinggui.recipes.msc.GlobalConstants.RECIPE_DETAIL_ID;

/**
 * Holds list of steps for a given recipe
 */
public class MethodTabFragment extends Fragment {

    FragmentMethodBinding binding;

    private static final int METHOD_LOADER = 121;

    private MethodViewAdapter methodStepsAdapter;
    private List<MethodStep> methodStepsList;

    private long recipeId;

    public MethodTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_method, container, false);
        View view = binding.getRoot();


        recipeId = getArguments().getLong(RECIPE_DETAIL_ID);

        getActivity().getSupportLoaderManager().initLoader(METHOD_LOADER, null, loaderCallbacks);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        methodStepsAdapter = new MethodViewAdapter();
        methodStepsList = new ArrayList<>();
        binding.rvwMethod.setAdapter(methodStepsAdapter);
        binding.rvwMethod.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onStop() {
        super.onStop();

        //Loader will always reload data in onStart, so reset here
        methodStepsList.clear();
        methodStepsAdapter.notifyDataSetChanged();
    }

    private LoaderManager.LoaderCallbacks<List<MethodStep>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<MethodStep>>() {
        @NonNull
        @Override
        public Loader<List<MethodStep>> onCreateLoader(int id, @Nullable Bundle args) {
            return new GetMethodStepsLoader(getActivity(), recipeId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<MethodStep>> loader, List<MethodStep> data) {
            methodStepsList = new ArrayList<>(data);
            methodStepsAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<MethodStep>> loader) {
            methodStepsList = new ArrayList<>(Collections.<MethodStep>emptyList());
            methodStepsAdapter.notifyDataSetChanged();
        }
    };

    class MethodViewAdapter extends RecyclerView.Adapter<MethodViewAdapter.StepViewHolder> {

        @Override
        public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new StepViewHolder(ViewMethodItemBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(StepViewHolder holder, int position) {
            MethodStep step = methodStepsList.get(position);
            holder.bind(step);
        }

        @Override
        public int getItemCount() {
            return methodStepsList.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {

            ViewMethodItemBinding binding;

            StepViewHolder(ViewMethodItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(MethodStep item) {
                binding.setMethodStep(item);
                binding.executePendingBindings();
            }
        }
    }
}
