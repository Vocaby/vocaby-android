package com.example.vocaby;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.SavesViewHolder> {
    private List<String> saves;
    private Context ctx;
    private OnItemTouchListener onItemTouchListener;

    public interface OnItemTouchListener {
        void onItemTouch(int position);
    }

    public SavesAdapter(Context ctx, List<String> saves, OnItemTouchListener onItemTouchListener) {
        this.saves = saves;
        this.onItemTouchListener = onItemTouchListener;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public SavesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        CardView card = (CardView) inflater.inflate(R.layout.save_item, parent, false);

        return new SavesViewHolder(card, onItemTouchListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavesAdapter.SavesViewHolder holder, int position) {
        holder.savedWord.setText(saves.get(position));
    }

    @Override
    public int getItemCount() {
        return saves.size();
    }

    public class SavesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView savedWord;
        OnItemTouchListener onItemTouchListener;
        public SavesViewHolder(@NonNull View itemView, OnItemTouchListener onItemTouchListener) {
            super(itemView);
            savedWord = itemView.findViewById(R.id.save_item);
            this.onItemTouchListener = onItemTouchListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemTouchListener.onItemTouch(getAdapterPosition());
        }
    }
}
