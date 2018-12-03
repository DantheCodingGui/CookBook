package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperAdapter;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperViewHolder;

/**
 * Group of utility callback methods to enable swipe and drag & drop features to RecyclerView
 */
public class AddItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter touchHelperAdapter;

    AddItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        touchHelperAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        touchHelperAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        touchHelperAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            ItemTouchHelperViewHolder helper = (ItemTouchHelperViewHolder) viewHolder;
            helper.onItemClear();
        }
    }
}
