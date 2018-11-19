package com.danthecodinggui.recipes.view.ItemTouchHelper;

/**
 * Allows ViewHolders to respond to swipe-to-dismiss events
 */
public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
