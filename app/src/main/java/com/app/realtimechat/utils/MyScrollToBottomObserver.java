package com.app.realtimechat.utils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

public class MyScrollToBottomObserver extends RecyclerView.AdapterDataObserver {
    private final RecyclerView recycler;
    private final FirebaseRecyclerAdapter adapter;
    private final LinearLayoutManager manager;

    public MyScrollToBottomObserver(@NotNull RecyclerView recycler, @NotNull FirebaseRecyclerAdapter adapter, @NotNull LinearLayoutManager manager) {
        super();
        this.recycler = recycler;
        this.adapter = adapter;
        this.manager = manager;
    }

    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        int count = this.adapter.getItemCount();
        int lastVisiblePosition = this.manager.findLastCompletelyVisibleItemPosition();
        boolean loading = lastVisiblePosition == -1;
        boolean atBottom = positionStart >= count - 1 && lastVisiblePosition == positionStart - 1;
        if (loading || atBottom) {
            this.recycler.scrollToPosition(positionStart);
        }

    }
}
