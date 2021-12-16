package com.vocaby.app.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.app.R
import com.vocaby.app.adapters.SearchHistoryAdapter.HistoryViewHolder
import com.vocaby.app.models.dictionary.SimpleEntryModel
import java.util.*

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