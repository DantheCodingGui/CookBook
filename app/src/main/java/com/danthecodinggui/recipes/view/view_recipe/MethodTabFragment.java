package com.danthecodinggui.recipes.view.view_recipe;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danthecodinggui.recipes.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Holds list of steps for a given recipe
 */
public class MethodTabFragment extends Fragment {

    @BindView(R.id.rvw_method)
    RecyclerView methodStepsView;
    private MethodViewAdapter methodStepsAdapter;
    private List<String> methodSteps;

    public MethodTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_method, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        methodStepsAdapter = new MethodViewAdapter();
        methodSteps = new ArrayList<>();
        methodStepsView.setAdapter(methodStepsAdapter);
        methodStepsView.setLayoutManager(new LinearLayoutManager(getContext()));

        methodSteps.add("Do first thing");
        methodSteps.add("Do second thing");
        methodSteps.add("Do third thing");
        methodSteps.add("Do fourth thing");
        methodSteps.add("Do fifth thing");

        methodStepsAdapter.notifyDataSetChanged();
    }

    class MethodViewAdapter extends RecyclerView.Adapter<MethodViewAdapter.StepViewHolder> {

        @Override
        public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StepViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.method_item, parent, false));
        }

        @Override
        public void onBindViewHolder(StepViewHolder holder, int position) {
            holder.step.setText(methodSteps.get(position));
        }

        @Override
        public int getItemCount() {
            return methodSteps.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txt_method_item)
            TextView step;

            StepViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
