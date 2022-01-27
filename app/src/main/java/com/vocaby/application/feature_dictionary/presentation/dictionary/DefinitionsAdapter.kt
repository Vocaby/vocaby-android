package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.presentation.dictionary.DefinitionsAdapter.DefinitionsViewHolder

class DefinitionsAdapter(private val ctx: Context) : RecyclerView.Adapter<DefinitionsViewHolder>() {
    private var entryData: EntryModel = EntryModel(entry = "Vocaby")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionsViewHolder {
        val inflater = LayoutInflater.from(ctx)
        val view = inflater.inflate(R.layout.item_definition_container, parent, false)
        return DefinitionsViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setWordData(entryData: EntryModel) {
        this.entryData = entryData
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: DefinitionsViewHolder, position: Int) {
        val group = entryData.definitionGroups[position]
        val definitions: List<DefinitionModel> = group.definitionData
        holder.pos.text = group.type
        val inflater = LayoutInflater.from(ctx)

        for (i in definitions.indices) {
            val def = definitions[i].definition
            val sen = definitions[i].example
            val card = inflater.inflate(R.layout.item_definition_row, null) as LinearLayout
            val definition = card.findViewById<TextView>(R.id.definition)
            val sentence = card.findViewById<TextView>(R.id.sentence)
            val counter = card.findViewById<TextView>(R.id.definition_counter)
            val exampleHeader = card.findViewById<TextView>(R.id.example_header)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams.setMargins(12, 8, 12, 8)
            card.layoutParams = layoutParams

            if (!sen.isNullOrEmpty()) {
                sentence.text = sen
            } else {
                sentence.visibility = View.GONE
                exampleHeader.visibility = View.GONE
            }

            val numbering = "${i + 1}. "
            counter.text = numbering
            definition.text = def
            holder.definitionContainer.addView(card)
        }
    }

    override fun getItemCount(): Int {
        return entryData.definitionGroups.size
    }

    class DefinitionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pos: TextView = itemView.findViewById(R.id.part_of_speech)
        var definitionContainer: LinearLayout = itemView.findViewById(R.id.definitions_card_container)
    }
}