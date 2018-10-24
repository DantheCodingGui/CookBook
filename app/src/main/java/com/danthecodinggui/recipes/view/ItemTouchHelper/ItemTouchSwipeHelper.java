package com.danthecodinggui.recipes.view.ItemTouchHelper;

/**
 * Allows ViewHolders to change appearance based on quantity of swipe when swipe-to-dismiss
 */
public interface ItemTouchSwipeHelper {
    /**
     *
     * @param percentSwiped The progress of the swipe
     */
    void onItemSwipe(float percentSwiped);
}
