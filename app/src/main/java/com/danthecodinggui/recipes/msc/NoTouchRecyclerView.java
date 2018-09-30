package com.danthecodinggui.recipes.msc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Simple subclass disabling click listener from RecyclerView, allowing the parent to handle it
 */
public class NoTouchRecyclerView extends RecyclerView {

    public NoTouchRecyclerView(Context context) {
        super(context);
    }

    public NoTouchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoTouchRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return super.onTouchEvent(e);
    }
}
