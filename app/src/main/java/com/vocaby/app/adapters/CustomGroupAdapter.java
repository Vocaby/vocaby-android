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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.vocaby.app.R;
import com.vocaby.app.models.dictionary.DefinitionGroupModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomGroupAdapter extends RecyclerView.Adapter<CustomGroupAdapter.CustomGroupViewHolder>
        implements ItemTouchHelperAdapter {
    List<DefinitionGroupModel> groups;
    DragStartListener dragStartListener;
    ItemInteractionListener itemInteractionListener;
    Context ctx;

    public interface ItemInteractionListener {
        void onGroupCardClicked(int position);
        void onItemRemoved(int position);
    }

    public CustomGroupAdapter(Context ctx, DragStartListener dragStartListener,
                              ItemInteractionListener itemInteractionListener) {
        this.ctx = ctx;
        this.dragStartListener = dragStartListener;
        this.itemInteractionListener = itemInteractionListener;

        groups = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<DefinitionGroupModel> newList) {
        groups = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_group_row, parent, false);
        return new CustomGroupViewHolder(view, ctx);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull CustomGroupViewHolder holder, int position) {
        holder.groupHeader.setText(groups.get(holder.getAdapterPosition()).getType());
        String counter = "" + groups.get(holder.getAdapterPosition()).getDefinitionData().size();
        holder.definitionCounter.setText(counter);

        if (groups.get(holder.getAdapterPosition()).getDefinitionData().size() > 1) {
            holder.definitionCounterHeader.setText(R.string.definition_header_plural);
        } else {
            holder.definitionCounterHeader.setText(R.string.definition_header_singular);
        }

        holder.dragHandle.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                dragStartListener.onDragStart(holder);
            }

            return false;
        });

        holder.container.setOnClickListener(v ->
                itemInteractionListener.onGroupCardClicked(holder.getAdapterPosition())
        );
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                groups.get(i).setOrder(i+1);
                groups.get(i+1).setOrder(i);
                Collections.swap(groups, i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                groups.get(i).setOrder(i-1);
                groups.get(i-1).setOrder(i);
                Collections.swap(groups, i, i-1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        if (position != -1) itemInteractionListener.onItemRemoved(position);
    }

    public void addItem() {
        notifyItemInserted(groups.size() - 1);
    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        notifyItemChanged(position);
    }

    public static class CustomGroupViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder {
        TextView groupHeader;
        TextView definitionCounter;
        TextView definitionCounterHeader;
        FrameLayout dragHandle;
        View container;
        Context ctx;

        public CustomGroupViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            groupHeader = itemView.findViewById(R.id.group_card_header);
            definitionCounter = itemView.findViewById(R.id.group_card_def_counter);
            definitionCounterHeader = itemView.findViewById(R.id.group_card_def_counter_header);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            container = itemView.findViewById(R.id.card_container);
            this.ctx = ctx;
        }

        @Override
        public void onItemDragged() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.colorSecondary));
        }

        @Override
        public void onItemSwiped() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.colorHeadline));
        }

        @Override
        public void onItemDone() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.light_gray));
        }
    }
}
