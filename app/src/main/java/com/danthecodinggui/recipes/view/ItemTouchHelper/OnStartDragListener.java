package com.danthecodinggui.recipes.view.ItemTouchHelper;

import android.support.v7.widget.RecyclerView;

/**
 * Allows adapter to programmatically initiate drag of ViewHolder
 */
public interface OnStartDragListener {

    /**
     * Called when a view is requesting a start of a drag.
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
