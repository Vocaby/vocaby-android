package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vocaby.app.R;

import java.util.ArrayList;
import java.util.List;

public class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.SavesViewHolder> {
    private List<String> saves;
    private final Context ctx;
    private final SaveItemTouchListener saveItemTouchListener;
    private final MaterialAlertDialogBuilder builder;

    public interface SaveItemTouchListener {
        void onItemDelete(String entry);
        void getDefinition(String entry);
    }

    public SavesAdapter(Context ctx, SaveItemTouchListener saveItemTouchListener, Activity activity) {
        this.saveItemTouchListener = saveItemTouchListener;
        this.ctx = ctx;
        this.saves = new ArrayList<>();
        builder = new MaterialAlertDialogBuilder(activity);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSavedWords(List<String> newSavedWords) {
        saves = newSavedWords;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        CardView card = (CardView) inflater.inflate(R.layout.save_item, parent, false);
        return new SavesViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull SavesAdapter.SavesViewHolder holder, int position) {
        String entry = saves.get(holder.getAdapterPosition());
        holder.savedWord.setText(entry);

        holder.itemView.setOnClickListener(v -> saveItemTouchListener.getDefinition(entry));

        holder.removeSaveButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you want to delete?")
                    .setMessage(entry)
                    .setPositiveButton("DELETE",
                            (dialog, which) -> saveItemTouchListener.onItemDelete(entry))
                    .setNegativeButton("CANCEL", null);

            showAlertDialog();
        });
    }

    public void addWordToRV() {
        notifyItemInserted(0);
    }

    public void removeWordFromRV(String entry) {
        int position = saves.indexOf(entry);
        if (position > -1) {
            notifyItemRemoved(position);
        }
    }

    private void showAlertDialog() {
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return saves.size();
    }

    public static class SavesViewHolder extends RecyclerView.ViewHolder {
        private final TextView savedWord;
        private final CardView cardView;
        ImageButton removeSaveButton;
        public SavesViewHolder(@NonNull View itemView) {
            super(itemView);
            savedWord = itemView.findViewById(R.id.save_item);
            removeSaveButton = itemView.findViewById(R.id.unsave_button);
            cardView = itemView.findViewById(R.id.card_container);
        }
    }
}
