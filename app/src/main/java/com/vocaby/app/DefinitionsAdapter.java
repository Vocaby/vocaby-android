package com.vocaby.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class DefinitionsAdapter extends RecyclerView.Adapter<DefinitionsAdapter.DefinitionsViewHolder> {
    private Word wordData;
    private String[] allowedPos;
    private Context ctx;

    public DefinitionsAdapter(Context ctx, Word wordData) {
        this.wordData = wordData;
        this.ctx = ctx;

        allowedPos = wordData.getAllowedPos();
    }

    @NonNull
    @Override
    public DefinitionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.section_row, parent, false);
        return new DefinitionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DefinitionsAdapter.DefinitionsViewHolder holder, int position) {
        String selectedPos = allowedPos[position];
        String[] definitions = wordData.getDefinitions(selectedPos);
        String[] sentences = wordData.getSentences(selectedPos);
        holder.pos.setText(selectedPos);

        LayoutInflater inflater = LayoutInflater.from(ctx);
        for(int i = 0; i < definitions.length; i++) {
            String def = definitions[i];
            String sen = sentences[i];
            CardView card;
            TextView definition;
            if(sen.length() > 0) {
                card = (CardView) inflater.inflate(R.layout.definition_row, null);
                definition = card.findViewById(R.id.definition);
                TextView sentence = card.findViewById(R.id.sentence);
                sentence.setText(sen);
            } else {
                card = (CardView) inflater.inflate(R.layout.definition_row_no_sentence, null);
                definition = card.findViewById(R.id.definition);
            }

            definition.setText(def);
            holder.definitionContainer.addView(card);
        }
    }

    @Override
    public int getItemCount() {
        return allowedPos.length;
    }

    public class DefinitionsViewHolder extends RecyclerView.ViewHolder {
        TextView pos;
        LinearLayout definitionContainer;

        public DefinitionsViewHolder(@NonNull View itemView) {
            super(itemView);
            pos = itemView.findViewById(R.id.part_of_speech);
            definitionContainer = itemView.findViewById(R.id.definitions_card_container);
        }
    }
}
