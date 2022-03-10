package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.domain.model.DailyPick

class EodListAdapter(
    private val ctx: Context,
    private val interaction: Interaction,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffCallback = object : DiffUtil.ItemCallback<DailyPick>() {
        override fun areItemsTheSame(oldItem: DailyPick, newItem: DailyPick): Boolean {
            return oldItem.entry == newItem.entry
        }

        override fun areContentsTheSame(oldItem: DailyPick, newItem: DailyPick): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EodAdapterViewHolder(
            ctx,
            LayoutInflater.from(parent.context).inflate(
                R.layout.card_eod,
                parent,
                false
            ),
            interaction,
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EodAdapterViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<DailyPick>) {
        differ.submitList(list)
    }

    class EodAdapterViewHolder
    constructor(
        private val ctx: Context,
        itemView: View,
        private val interaction: Interaction,
    ) : RecyclerView.ViewHolder(itemView) {
        private val entryView: TextView = itemView.findViewById(R.id.entry_header)
        private val typeView: TextView = itemView.findViewById(R.id.type)
        private val definition: TextView = itemView.findViewById(R.id.card_definition)
        private val sentence: TextView = itemView.findViewById(R.id.card_sentence)
        private val wordBoxTag: TextView = itemView.findViewById(R.id.word_box_tag)

        fun bind(pick: DailyPick) {
            entryView.text = pick.entry
            typeView.text =  pick.type
            definition.text = pick.definition

            pick.example?.let {
                if (it.isNotEmpty()) {
                    sentence.text = it
                }
            }

            if (pick.random) {
                wordBoxTag.setText(R.string.wod_random_pick)
                wordBoxTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                wordBoxTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadlineSoft))
            } else {
                wordBoxTag.setText(R.string.wod_our_pick)
                wordBoxTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimaryAccent))
                wordBoxTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorSecondary))
            }

            itemView.setOnClickListener {
                interaction.onItemTouch(pick.entry)
            }
        }
    }

    interface Interaction {
        fun onItemTouch(entry: String)
    }
}