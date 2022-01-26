package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.domain.model.SimpleEntryModel
import com.vocaby.application.feature_dictionary.presentation.dictionary.SearchHistoryAdapter.HistoryViewHolder

class SearchHistoryAdapter(
    private val ctx: Context,
    private val onItemTouchListener: OnItemTouchListener
) : RecyclerView.Adapter<HistoryViewHolder>() {
    private var history: List<SimpleEntryModel> = ArrayList()

    interface OnItemTouchListener {
        fun onItemTouch(position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchHistory(newHistory: List<SimpleEntryModel>) {
        history = newHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            LayoutInflater.from(ctx).inflate(R.layout.history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            onItemTouchListener.onItemTouch(
                holder.adapterPosition
            )
        }

        holder.word.text = history[position].entry
        holder.definition.text = history[position].definition
    }

    override fun getItemCount(): Int = history.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var word: TextView = itemView.findViewById(R.id.history_item)
        var definition: TextView = itemView.findViewById(R.id.history_definition)
    }
}