package com.danthecodinggui.recipes.msc;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Linear layout wrapper that doesn't scroll vertically
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
