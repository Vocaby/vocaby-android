package com.vocaby.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vocaby.app.R;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.models.EntryModel;

import java.util.List;

public class DefinitionsAdapter extends RecyclerView.Adapter<DefinitionsAdapter.DefinitionsViewHolder> {
    private EntryModel entryData;
    private final Context ctx;

    public DefinitionsAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public DefinitionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.section_row, parent, false);
        return new DefinitionsViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setWordData(EntryModel entryData) {
        this.entryData = entryData;
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull DefinitionsAdapter.DefinitionsViewHolder holder, int position) {
        if(entryData != null) {
            DefinitionGroupModel group = entryData.getDefinitionGroups().get(position);
            List<DefinitionModel> definitions = group.getDefinitionData();
            holder.pos.setText(group.getType());

            LayoutInflater inflater = LayoutInflater.from(ctx);
            for(int i = 0; i < definitions.size(); i++) {
                String def = definitions.get(i).getDefinition();
                String sen = definitions.get(i).getExample();
                CardView card = (CardView) inflater.inflate(R.layout.definition_row, null);
                TextView definition = card.findViewById(R.id.definition);
                TextView sentence = card.findViewById(R.id.sentence);
                TextView counter = card.findViewById(R.id.definition_counter);

                if(sen.length() > 0) {
                    sentence.setText(sen);
                } else {
                    sentence.setVisibility(View.GONE);
                }

                String numbering = i+1 + ". ";
                counter.setText(numbering);
                definition.setText(def);
                holder.definitionContainer.addView(card);
            }
        }
    }

    @Override
    public int getItemCount() {
        return entryData == null ? 0 : entryData.getDefinitionGroups().size();
    }

    public static class DefinitionsViewHolder extends RecyclerView.ViewHolder {
        TextView pos;
        LinearLayout definitionContainer;

        public DefinitionsViewHolder(@NonNull View itemView) {
            super(itemView);
            pos = itemView.findViewById(R.id.part_of_speech);
            definitionContainer = itemView.findViewById(R.id.definitions_card_container);
        }
    }
}
