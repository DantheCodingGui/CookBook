package com.danthecodinggui.recipes.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.LinearLayout;

/**
 * Created by Dan on 12/03/2018.
 */
public class NoScrollLinearLayout extends LinearLayoutManager {
    public NoScrollLinearLayout(Context context) {
        super(context);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
