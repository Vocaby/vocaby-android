package com.vocaby.application.feature_dictionary_custom.presentation.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import java.util.*

class CustomEntryAdapter(
    private val interaction: Interaction
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var customEntries: List<UserEntry?> = LinkedList()
    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_ITEM = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ITEM) {
            return CustomEntryViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_custom_entry,
                    parent,
                    false
                ),
                interaction
            )
        } else {
            return CustomEntryLoadingViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_custom_entry_loading,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (customEntries[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomEntryViewHolder -> customEntries[position]?.let {
                holder.bind(it)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: LinkedList<UserEntry?>) {
        customEntries = list
        notifyDataSetChanged()
    }

    fun addEntry() {
        notifyItemInserted(0)
    }

    fun addEntryLast() {
        notifyItemInserted(itemCount-1)
    }

    fun addEntryRange(low: Int, high: Int) {
        notifyItemRangeChanged(low, high)
    }

    fun deleteEntry(position: Int) {
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return customEntries.size
    }

    class CustomEntryViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction,
    ): RecyclerView.ViewHolder(itemView) {
        private val card: CardView = itemView.findViewById(R.id.card_container)
        private val header: TextView = itemView.findViewById(R.id.custom_entry_item_header)
        private val lastUpdated: TextView = itemView.findViewById(R.id.custom_entry_item_updated)
        private val entryType: TextView = itemView.findViewById(R.id.custom_entry_type)
        private val moreButton: ImageButton = itemView.findViewById(R.id.more_button)

        fun bind(userEntry: UserEntry) {
            header.text = userEntry.entry
            lastUpdated.text = Formatter.formatDateToString(userEntry.lastUpdated.time, forDisplay = true, showDay = false)
            entryType.text = Formatter.firstLetterUpperCase(userEntry.type)

            card.setOnClickListener {
                interaction.onItemTouch(userEntry.entry, adapterPosition)
            }

            moreButton.setOnClickListener {
                interaction.onItemUpdate(userEntry.entry, adapterPosition)
            }
        }
    }

    class CustomEntryLoadingViewHolder
    constructor(
        itemView: View,
    ): RecyclerView.ViewHolder(itemView) {
    }

    interface Interaction {
        fun onItemUpdate(entry: String, position: Int)
        fun onItemTouch(entry: String, position: Int)
    }
}