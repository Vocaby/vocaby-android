package com.vocaby.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.HistoryViewHolder> {
    private final List<String> history;
    private final Context ctx;
    private final OnItemTouchListener onItemTouchListener;

    public interface OnItemTouchListener {
        void onItemTouch(int position);
    }

    public SearchAdapter(Context ctx, List<String> history, OnItemTouchListener onItemTouchListener) {
        this.ctx = ctx;
        this.history = history;
        this.onItemTouchListener = onItemTouchListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view, onItemTouchListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.HistoryViewHolder holder, int position) {
        holder.word.setText(history.get(position));
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView word;
        OnItemTouchListener onItemTouchListener;

        public HistoryViewHolder(@NonNull View itemView, OnItemTouchListener onItemTouchListener) {
            super(itemView);
            word = itemView.findViewById(R.id.history_item);
            this.onItemTouchListener = onItemTouchListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemTouchListener.onItemTouch(getAdapterPosition());
        }
    }
}