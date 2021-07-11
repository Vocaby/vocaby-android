package com.example.vocaby;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

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
        int counter = 0;

        LayoutInflater inflater = LayoutInflater.from(ctx);
        for(int i = 0; i < definitions.length; i++) {
            String def = definitions[i];
            String sen = sentences[i];
            if(sen.length() > 0) {
                CardView card = (CardView) inflater.inflate(R.layout.definition_row, null);
                if(counter % 2 == 0) {
                    card.setCardBackgroundColor(ctx.getColor(R.color.light_gray));
                }
                TextView definition = card.findViewById(R.id.definition);
                TextView sentence = card.findViewById(R.id.sentence);
                definition.setText(def);
                sentence.setText(sen);
                holder.definitionContainer.addView(card);
                counter++;
            }
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
