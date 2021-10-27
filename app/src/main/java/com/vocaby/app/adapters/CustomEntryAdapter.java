package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vocaby.app.R;

import java.util.ArrayList;
import java.util.List;

public class CustomEntryAdapter extends RecyclerView.Adapter<CustomEntryAdapter.EntryViewHolder> {
    private List<String> customEntries;
    private final ItemTouchListener itemTouchListener;
    private final MaterialAlertDialogBuilder builder;

    public interface ItemTouchListener {
        void onItemDelete(String entry, int position);
        void onItemTouch(String entry, int position);
    }

    public CustomEntryAdapter(Activity activity, ItemTouchListener itemTouchListener) {
        customEntries = new ArrayList<>();
        this.itemTouchListener = itemTouchListener;
        builder = new MaterialAlertDialogBuilder(activity);
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_entry_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        String entry = customEntries.get(holder.getAdapterPosition());

        holder.itemView.setOnClickListener(view -> {
            // You want to use getAdapterPosition instead of position
            // in the listener because onBindViewHolder will not be called again
            // from structural changes.
            itemTouchListener.onItemTouch(
                    entry,
                    holder.getAdapterPosition()
            );
        });

        holder.removeEntryButton.setOnClickListener(v -> {
            builder.setTitle("Are you sure you want to delete?")
                    .setMessage(entry)
                    .setPositiveButton("DELETE", (dialog, which) ->
                            itemTouchListener.onItemDelete(entry, holder.getAdapterPosition()))
                    .setNegativeButton("CANCEL", null);

            showAlertDialog();
        });

        holder.header.setText(customEntries.get(position));
    }

    private void showAlertDialog() {
        AlertDialog alert = builder.create();
        alert.show();
    }

    // The list is only set on create
    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<String> newList) {
        customEntries = newList;
        notifyDataSetChanged();
    }

    public void addEntry() {
        notifyItemInserted(0);
    }

    public void deleteEntry(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return customEntries.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        ImageButton removeEntryButton;
        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.custom_entry_item_header);
            removeEntryButton = itemView.findViewById(R.id.delete_button);
        }
    }

}
