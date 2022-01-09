package com.vocaby.app.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.app.R
import com.vocaby.app.models.customentry.UserEntry
import com.vocaby.app.utils.Formatter

class CustomEntryAdapter(activity: Activity, private val interaction: Interaction) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {
    private var customEntries: List<UserEntry> = ArrayList()
    private val alertDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomEntryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.custom_entry_item,
                parent,
                false
            ),
            interaction,
            alertDialogBuilder
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomEntryViewHolder -> holder.bind(customEntries[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<UserEntry>) {
        customEntries = list
        notifyDataSetChanged()
    }

    fun addEntry() {
        notifyItemInserted(0)
    }

    fun deleteEntry(position: Int) {
        notifyItemRemoved(position)
    }

    fun updateEntry(position: Int) {
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return customEntries.size
    }

    class CustomEntryViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction,
        private val alertDialogBuilder: MaterialAlertDialogBuilder
    ): RecyclerView.ViewHolder(itemView) {
        private val header: TextView = itemView.findViewById(R.id.custom_entry_item_header)
        private val lastUpdated: TextView = itemView.findViewById(R.id.custom_entry_item_updated)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(userEntry: UserEntry) {
            header.text = userEntry.entry
            lastUpdated.text = Formatter.formatDateStringForDisplay(userEntry.lastUpdated)

            itemView.setOnClickListener {
                interaction.onItemTouch(userEntry.entry, adapterPosition)
            }

            deleteButton.setOnClickListener {
                alertDialogBuilder
                    .setTitle("Are you sure you want to delete?")
                    .setMessage(userEntry.entry)
                    .setPositiveButton("DELETE") { _, _ ->
                        interaction.onItemDelete(userEntry.entry, adapterPosition)
                    }.setNegativeButton("CANCEL", null).create().show()
            }
        }
    }

    interface Interaction {
        fun onItemTouch(entry: String, position: Int)
        fun onItemDelete(entry: String, position: Int)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemDismiss(position: Int) {
        alertDialogBuilder
            .setTitle("Are you sure you want to delete?")
            .setMessage(customEntries[position].entry)
            .setPositiveButton("DELETE") { _, _ ->
                interaction.onItemDelete(customEntries[position].entry, position)
            }.setNegativeButton("CANCEL") { _, _ -> notifyItemChanged(position) }.create().show()
    }
}