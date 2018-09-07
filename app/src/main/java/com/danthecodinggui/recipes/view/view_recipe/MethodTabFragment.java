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
import com.danthecodinggui.recipes.view.Loaders.GetMethodStepsLoader;
import com.danthecodinggui.recipes.view.Loaders.UpdatingAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.danthecodinggui.recipes.msc.IntentConstants.RECIPE_DETAIL_ID;

/**
 * Holds list of steps for a given recipe
 */
public class MethodTabFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<String>>,
        UpdatingAsyncTaskLoader.ProgressUpdateListener {

    private static final int METHOD_LOADER = 201;

    @BindView(R.id.rvw_method)
    RecyclerView methodStepsView;
    private MethodViewAdapter methodStepsAdapter;
    private List<String> methodList;

    private long recipeId;

    public MethodTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_method, container, false);

        ButterKnife.bind(this, view);

        recipeId = getArguments().getLong(RECIPE_DETAIL_ID);

        getActivity().getSupportLoaderManager().initLoader(METHOD_LOADER, null, this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        methodStepsAdapter = new MethodViewAdapter();
        methodList = new ArrayList<>();
        methodStepsView.setAdapter(methodStepsAdapter);
        methodStepsView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @NonNull
    @Override
    public Loader<List<String>> onCreateLoader(int id, @Nullable Bundle args) {
        Handler uiThread = new Handler(Looper.getMainLooper());
        return new GetMethodStepsLoader(getActivity(), uiThread, this, id, recipeId);
    }

    @Override
    public <T> void onProgressUpdate(int loaderId, T updateValue) {
        methodList.addAll((List)updateValue);
        methodStepsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<String>> loader, List<String> remainingSteps) {
        methodList.addAll(remainingSteps);
        methodStepsAdapter.notifyDataSetChanged();

        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<String>> loader) {}

    class MethodViewAdapter extends RecyclerView.Adapter<MethodViewAdapter.StepViewHolder> {

        @Override
        public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StepViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.method_item, parent, false));
        }

        @Override
        public void onBindViewHolder(StepViewHolder holder, int position) {
            holder.step.setText(methodList.get(position));
        }

        @Override
        public int getItemCount() {
            return methodList.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.crd_method_item)
            CardView background;

            @BindView(R.id.txt_method_item)
            TextView step;

            StepViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                background.setCardBackgroundColor(getResources().getColor(R.color.colorMethodStep));
            }
        }
    }
}
