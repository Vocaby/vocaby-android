package com.vocaby.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;


import java.nio.charset.StandardCharsets;
import java.util.List;

public class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.SavesViewHolder> {
    private final List<String> saves;
    private final Context ctx;
    private final OnItemTouchListener onItemTouchListener;
    private final DataManager dataManager;
    private final AlertDialog.Builder builder;
    private String token;

    public interface OnItemTouchListener {
        void onItemTouch(int position);
    }

    public SavesAdapter(Context ctx, List<String> saves, OnItemTouchListener onItemTouchListener, Activity activity) {
        this.saves = saves;
        this.onItemTouchListener = onItemTouchListener;
        this.ctx = ctx;
        dataManager = DataManager.getInstance(ctx);

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
        holder.savedWord.setText(word);
        holder.removeSaveButton.setOnClickListener(v -> {
            builder.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", (dialog, which) -> {
                if(token.isEmpty()) {
                    dataManager.deleteSave(word);
                    notifyDataSetChanged();
                } else {
                    RequestManager requestManager = RequestManager.getInstance(ctx);
                    requestManager.makeDeleteRequest(word, response -> {
                        dataManager.deleteSave(word);
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
        OnItemTouchListener onItemTouchListener;
        ImageButton removeSaveButton;
        public SavesViewHolder(@NonNull View itemView, OnItemTouchListener onItemTouchListener) {
            super(itemView);
            savedWord = itemView.findViewById(R.id.save_item);
            removeSaveButton = itemView.findViewById(R.id.unsave_button);
            this.onItemTouchListener = onItemTouchListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemTouchListener.onItemTouch(getAdapterPosition());
        }
    }
}
