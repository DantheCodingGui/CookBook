package com.danthecodinggui.recipes.view.ItemTouchHelper;

/**
 * Defines methods that the ItemTouchHelper callback subclass will redirect to the
 * RecyclerViewAdapter
 */
public interface ItemTouchHelperAdapter {
    void onItemDismiss(int position);
}
