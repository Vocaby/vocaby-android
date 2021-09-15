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
import com.vocaby.app.models.DefinitionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomDefAdapter extends RecyclerView.Adapter<CustomDefAdapter.CustomDefViewHolder>
        implements ItemTouchHelperAdapter {
    List<DefinitionModel> definitions;
    DragStartListener dragStartListener;
    ItemInteractionListener itemInteractionListener;
    Context ctx;

    public interface ItemInteractionListener {
        void onItemRemoved(int position);
    }

    public CustomDefAdapter(Context ctx, DragStartListener dragStartListener,
                            ItemInteractionListener itemInteractionListener) {
        this.ctx = ctx;
        this.dragStartListener = dragStartListener;
        this.itemInteractionListener = itemInteractionListener;
        definitions = new ArrayList<>();
    }

    @NonNull
    @Override
    public CustomDefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_definition_row, parent, false);
        return new CustomDefViewHolder(view, ctx);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull CustomDefViewHolder holder, int position) {
        holder.definitionView.setText(definitions.get(position).getDefinition());
        String example = definitions.get(position).getExample();
        if (example.isEmpty()) {
            holder.exampleView.setVisibility(View.GONE);
        } else {
            holder.exampleView.setText(example);
        }

        holder.dragHandle.setOnTouchListener((v, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                dragStartListener.onDragStart(holder);
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return definitions.size();
    }

    public void setList(List<DefinitionModel> newList) {
        // soft copy
        definitions = newList;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(definitions, i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(definitions, i, i-1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        itemInteractionListener.onItemRemoved(position);
    }

    public void addItem() {
        notifyItemInserted(getItemCount()-1);
    }

    public static class CustomDefViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        TextView definitionView;
        TextView exampleView;
        FrameLayout dragHandle;
        Context ctx;
        public CustomDefViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            definitionView = itemView.findViewById(R.id.definition);
            exampleView = itemView.findViewById(R.id.example);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            this.ctx = ctx;
        }

        @Override
        public void onItemDragged() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.colorPrimary_sub));
        }

        @Override
        public void onItemSwiped() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.color_tertiary));
        }

        @Override
        public void onItemDone() {
            ((MaterialCardView) itemView).setStrokeColor(ctx.getColor(R.color.light_gray));
        }
    }
}
