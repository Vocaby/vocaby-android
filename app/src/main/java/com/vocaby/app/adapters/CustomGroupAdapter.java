package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.vocaby.app.R;
import com.vocaby.app.models.CustomEntryGroupModel;

import java.util.Collections;
import java.util.List;

public class CustomGroupAdapter extends RecyclerView.Adapter<CustomGroupAdapter.CustomDefViewHolder>
        implements ItemTouchHelperAdapter {
    List<CustomEntryGroupModel> groups;
    DragStartListener dragStartListener;
    ItemInteractionListener itemInteractionListener;
    Context ctx;

    public interface ItemInteractionListener {
        void onItemClicked(int position);
    }

    public CustomGroupAdapter(Context ctx, LiveData<List<CustomEntryGroupModel>> groupsLiveData,
                              LifecycleOwner lifecycleOwner, DragStartListener dragStartListener,
                              ItemInteractionListener itemInteractionListener) {
        this.ctx = ctx;
        this.dragStartListener = dragStartListener;
        this.itemInteractionListener = itemInteractionListener;

        groupsLiveData.observe(lifecycleOwner, groupsList -> {
            groups = groupsList;
        });
    }

    @NonNull
    @Override
    public CustomDefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_group_row, parent, false);
        return new CustomDefViewHolder(view, ctx);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull CustomDefViewHolder holder, int position) {
        holder.groupHeader.setText(groups.get(position).getType());
        String counter = "" + groups.get(position).getDefinitionData().size();
        holder.definitionCounter.setText(counter);

        holder.dragHandle.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                dragStartListener.onDragStart(holder);
            }

            return false;
        });

        holder.card.setOnClickListener(v -> {
            itemInteractionListener.onItemClicked(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(groups, i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(groups, i, i-1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    public void addItem() {
        notifyItemInserted(groups.size() - 1);
    }

    public void editItem(int position) {
        notifyItemChanged(position);
    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public static class CustomDefViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        TextView groupHeader;
        TextView definitionCounter;
        FrameLayout dragHandle;
        View itemView;
        MaterialCardView card;
        Context ctx;
        public CustomDefViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            this.itemView = itemView;
            groupHeader = itemView.findViewById(R.id.group_card_header);
            definitionCounter = itemView.findViewById(R.id.group_card_def_counter);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            card = itemView.findViewById(R.id.group_card);
            this.ctx = ctx;
        }

        @Override
        public void onItemSelected() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.colorPrimary_sub));
        }

        @Override
        public void onItemDone() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.light_gray));
        }
    }
}
