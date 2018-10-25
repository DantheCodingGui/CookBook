package com.danthecodinggui.recipes.view.ItemTouchHelper;

/**
 * Implemented by ViewHolders to allow appearance changes of CardView when in motion
 */
public interface ItemTouchHelperViewHolder {
    void onItemSelected(int actionState);
    void onItemClear();
}
