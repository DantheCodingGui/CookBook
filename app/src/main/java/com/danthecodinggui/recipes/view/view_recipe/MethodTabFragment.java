package com.danthecodinggui.recipes.view.view_recipe;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danthecodinggui.recipes.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MethodTabFragment extends Fragment {


    public MethodTabFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_method, container, false);
    }

}
