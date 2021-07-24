package com.vocaby.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.HistoryViewHolder> {
    private List<String> history;
    private Context ctx;

    public SearchAdapter(Context ctx, List<String> history) {
        this.ctx = ctx;
        this.history = history;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.HistoryViewHolder holder, int position) {
        holder.word.setText(history.get(position));
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView word;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.history_item);
        }
    }
}