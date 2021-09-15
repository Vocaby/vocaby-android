package com.vocaby.app.adapters;

import androidx.recyclerview.widget.RecyclerView;

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
