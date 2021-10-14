package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        void onItemDelete(String entry, int size);
        void getDefinition(int position);
        void onLastItemDeleted();
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
        if (newSavedWords.isEmpty()) saveItemTouchListener.onLastItemDeleted();
    }

    @NonNull
    @Override
    public SavesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        CardView card = (CardView) inflater.inflate(R.layout.save_item, parent, false);
        return new SavesViewHolder(card, saveItemTouchListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavesAdapter.SavesViewHolder holder, int position) {
        holder.cardView.setBackgroundTintList(ColorStateList.valueOf(ctx.getColor(R.color.very_light_gray)));
        String word = saves.get(holder.getAdapterPosition());
        holder.savedWord.setText(word);
        holder.removeSaveButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you want to delete?")
                    .setMessage(word)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        removeWord(holder.getAdapterPosition());
                    }).setNegativeButton("No", null);

            showAlertDialog();
        });
    }

    public void removeWord(int position) {
        saveItemTouchListener.onItemDelete(saves.get(position), saves.size());
        saves.remove(position);
        notifyItemRemoved(position);

        if (saves.size() == 0) saveItemTouchListener.onLastItemDeleted();
    }

    private void showAlertDialog() {
        AlertDialog alert = builder.create();
        alert.show();
        Button negativeButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ctx.getColor(R.color.colorHeadline));

    }

    @Override
    public int getItemCount() {
        return saves.size();
    }

    public static class SavesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView savedWord;
        private final CardView cardView;
        OnSaveItemButtonTouch onSaveItemButtonTouch;
        ImageButton removeSaveButton;
        SaveItemTouchListener saveItemTouchListener;
        public SavesViewHolder(@NonNull View itemView, SaveItemTouchListener saveItemTouchListener) {
            super(itemView);
            savedWord = itemView.findViewById(R.id.save_item);
            removeSaveButton = itemView.findViewById(R.id.unsave_button);
            cardView = itemView.findViewById(R.id.card_container);
            this.saveItemTouchListener = saveItemTouchListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            saveItemTouchListener.getDefinition(getAdapterPosition());
        }
    }
}
