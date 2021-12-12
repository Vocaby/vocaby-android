package com.vocaby.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vocaby.app.R;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.HistoryViewHolder> {
    private List<String> history;
    private final Context ctx;
    private final OnItemTouchListener onItemTouchListener;

    private static final int STATIC_CARD = 0;
    private static final int DYNAMIC_CARD = 1;

    public interface OnItemTouchListener {
        void onItemTouch(int position);
    }

    public SearchHistoryAdapter(Context ctx, OnItemTouchListener onItemTouchListener) {
        this.ctx = ctx;
        this.onItemTouchListener = onItemTouchListener;
        history = new ArrayList<>();
    }

    public void updateSearchHistory(List<String> newHistory) {
        if(newHistory.size() == 0)
            newHistory.add("HISTORY");

        this.history = newHistory;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view;
        if (viewType == STATIC_CARD) {
            view = inflater.inflate(R.layout.history_static_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.history_item, parent, false);
        }

        return new HistoryViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return STATIC_CARD;
        } else {
            return DYNAMIC_CARD;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryAdapter.HistoryViewHolder holder, int position) {
        if (holder.getAdapterPosition() != 0)
            holder.itemView.setOnClickListener(v -> {
                onItemTouchListener.onItemTouch(holder.getAdapterPosition());
            });
        holder.word.setText(history.get(position));
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView word;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.history_item);
        }
    }
}