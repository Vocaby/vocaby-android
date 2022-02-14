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
import com.vocaby.application.core.util.Logger
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import java.util.*

class CustomEntryAdapter(
    private val interaction: Interaction
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var customEntries: List<UserEntry> = LinkedList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomEntryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_custom_entry,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomEntryViewHolder -> holder.bind(customEntries[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: LinkedList<UserEntry>) {
        customEntries = list
        notifyDataSetChanged()
        Logger.reportToDebug("Dataset Changed: ${customEntries.hashCode()}")
    }

    fun addEntry() {
        Logger.reportToDebug("Adding item to adapter...: ${customEntries.hashCode()}")
        notifyItemInserted(0)
    }

    fun deleteEntry(position: Int) {
        Logger.reportToDebug("Removing item from adapter...: ${customEntries.hashCode()}")
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
        private val moreButton: ImageButton = itemView.findViewById(R.id.more_button)

        fun bind(userEntry: UserEntry) {
            header.text = userEntry.entry
            lastUpdated.text = Formatter.formatDateToString(userEntry.lastUpdated.time, forDisplay = true, showDay = false)

            card.setOnClickListener {
                interaction.onItemTouch(userEntry.entry, adapterPosition)
            }

            moreButton.setOnClickListener {
                interaction.onItemUpdate(userEntry.entry, adapterPosition)
            }
        }
    }

    interface Interaction {
        fun onItemUpdate(entry: String, position: Int)
        fun onItemTouch(entry: String, position: Int)
    }
}