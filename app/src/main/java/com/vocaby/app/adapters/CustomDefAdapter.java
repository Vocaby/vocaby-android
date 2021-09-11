package com.vocaby.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vocaby.app.R;
import com.vocaby.app.models.DefinitionModel;

import java.util.List;

public class CustomDefAdapter extends RecyclerView.Adapter<CustomDefAdapter.CustomDefViewHolder> {
    List<DefinitionModel> definitions;

    public CustomDefAdapter(List<DefinitionModel> definitions) {
        this.definitions = definitions;
    }

    @NonNull
    @Override
    public CustomDefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_definition_row, parent, false);
        return new CustomDefViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomDefViewHolder holder, int position) {
        holder.definitionView.setText(definitions.get(position).getDefinition());
        String example = definitions.get(position).getExample();
        if (example.isEmpty()) {
            holder.exampleView.setVisibility(View.GONE);
        } else {
            holder.exampleView.setText(example);
        }
    }

    @Override
    public int getItemCount() {
        return definitions.size();
    }

    public static class CustomDefViewHolder extends RecyclerView.ViewHolder {
        TextView definitionView;
        TextView exampleView;
        public CustomDefViewHolder(@NonNull View itemView) {
            super(itemView);
            definitionView = itemView.findViewById(R.id.definition);
            exampleView = itemView.findViewById(R.id.example);
        }
    }
}
