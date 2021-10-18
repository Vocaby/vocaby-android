package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vocaby.app.R;

import java.util.ArrayList;
import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeViewHolder> {
    private List<String> types;
    private int lastCheckedPosition = -1;
    private final ItemInteractionListener itemInteractionListener;

    public interface ItemInteractionListener {
        void onTypeClicked(String type);
    }

    public TypeAdapter(ItemInteractionListener itemInteractionListener) {
        types = new ArrayList<>();
        this.itemInteractionListener = itemInteractionListener;
    }

    public void addItem(String type) {
        types.add(0, type);
        notifyItemInserted(0);
    }

    public void removeItem(String type) {
        int position = types.indexOf(type);
        if (position != -1) {
            types.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void resetSelect() {
        lastCheckedPosition = -1;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<String> newList) {
        types = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.type_item, parent, false);
        return new TypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        holder.typeHeader.setText(types.get(holder.getAdapterPosition()));

        holder.cardView.setOnClickListener(v -> {
            lastCheckedPosition = holder.getAdapterPosition();
            itemInteractionListener.onTypeClicked(types.get(lastCheckedPosition));
            notifyDataSetChanged();
        });

        holder.cardView.setSelected(position == lastCheckedPosition);
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public static class TypeViewHolder extends RecyclerView.ViewHolder {
        TextView typeHeader;
        CardView cardView;

        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            typeHeader = itemView.findViewById(R.id.type_header);
            cardView = itemView.findViewById(R.id.card_container);
        }
    }
}
