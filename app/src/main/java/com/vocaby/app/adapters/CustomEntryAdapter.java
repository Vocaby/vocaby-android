package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vocaby.app.R;
import com.vocaby.app.data.entity.CustomEntry;

import java.util.ArrayList;
import java.util.List;

public class CustomEntryAdapter extends RecyclerView.Adapter<CustomEntryAdapter.EntryViewHolder> {
    private List<CustomEntry> customEntries;
    private ItemTouchListener itemTouchListener;

    public interface ItemTouchListener {
        void onItemTouch(CustomEntry entry);
    }

    public CustomEntryAdapter(ItemTouchListener itemTouchListener) {
        customEntries = new ArrayList<>();
        this.itemTouchListener = itemTouchListener;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_entry_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        holder.itemView.setOnClickListener(view -> {
            // You want to use getAdapterPosition instead of position
            // in the listener because onBindViewHolder will not be called again
            // from structural changes.
            itemTouchListener.onItemTouch(customEntries.get(holder.getAdapterPosition()));
        });

        holder.header.setText(customEntries.get(position).getEntry());
    }

    @SuppressLint("NotifyDataSetChanged")
    // The list is only set on create
    public void setList(List<CustomEntry> newList) {
        customEntries = newList;
        notifyDataSetChanged();
    }

    public void addEntry(int entryId, String entry) {
        customEntries.add(0, new CustomEntry(entryId, 0, entry, System.currentTimeMillis()));
        notifyItemInserted(0);
    }

    @Override
    public int getItemCount() {
        return customEntries.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.custom_entry_item_header);
        }
    }

}
