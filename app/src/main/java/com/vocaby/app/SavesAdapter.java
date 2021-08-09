package com.vocaby.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;


import java.nio.charset.StandardCharsets;
import java.util.List;

public class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.SavesViewHolder> {
    private List<String> saves;
    private final Context ctx;
    private final OnItemTouchListener onItemTouchListener;
    private final DataManager dataManager;
    private final AlertDialog.Builder builder;
    private String token;

    public interface OnItemTouchListener {
        void onItemTouch(int position);
        void onSaveDelete(int size);
    }

    public SavesAdapter(Context ctx, OnItemTouchListener onItemTouchListener, Activity activity) {
        this.onItemTouchListener = onItemTouchListener;
        this.ctx = ctx;
        dataManager = DataManager.getInstance(ctx);
        this.saves = dataManager.getSaves();
        builder = new AlertDialog.Builder(activity);
    }

    @NonNull
    @Override
    public SavesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        CardView card = (CardView) inflater.inflate(R.layout.save_item, parent, false);
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.token_key), Context.MODE_PRIVATE);
        token = sharedPref.getString(ctx.getString(R.string.token_key), "");
        return new SavesViewHolder(card, onItemTouchListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavesAdapter.SavesViewHolder holder, int position) {
        String word = saves.get(position);
        if (position % 2 == 0) holder.cardView.setBackgroundTintList(ColorStateList.valueOf(ctx.getColor(R.color.light_gray)));
        holder.savedWord.setText(word);
        holder.removeSaveButton.setOnClickListener(v -> {
            builder.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", (dialog, which) -> {
                if(token.isEmpty()) {
                    saves = dataManager.deleteSave(word);
                    onItemTouchListener.onSaveDelete(saves.size());
                    notifyDataSetChanged();
                } else {
                    RequestManager requestManager = RequestManager.getInstance(ctx);
                    requestManager.makeDeleteRequest(word, response -> {
                        saves = dataManager.deleteSave(word);
                        onItemTouchListener.onSaveDelete(saves.size());
                        notifyDataSetChanged();
                    }, error -> {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Toast.makeText(ctx, body, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ctx, "Something went wrong while removing...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).setNegativeButton("No", null);

            showAlertDialog();
        });
    }

    private void showAlertDialog() {
        AlertDialog alert = builder.create();
        alert.show();
        Button negativeButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTextColor(ctx.getColor(R.color.color_tertiary));

    }

    @Override
    public int getItemCount() {
        return saves.size();
    }

    public static class SavesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView savedWord;
        private final CardView cardView;
        OnItemTouchListener onItemTouchListener;
        ImageButton removeSaveButton;
        public SavesViewHolder(@NonNull View itemView, OnItemTouchListener onItemTouchListener) {
            super(itemView);
            savedWord = itemView.findViewById(R.id.save_item);
            removeSaveButton = itemView.findViewById(R.id.unsave_button);
            cardView = itemView.findViewById(R.id.card_container);
            this.onItemTouchListener = onItemTouchListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemTouchListener.onItemTouch(getAdapterPosition());
        }
    }
}
