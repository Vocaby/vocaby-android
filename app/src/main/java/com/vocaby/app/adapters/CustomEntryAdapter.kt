package com.vocaby.app.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.app.R

class CustomEntryAdapter(private val activity: Activity, private val interaction: Interaction) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var customEntries: List<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomEntryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.custom_entry_item,
                parent,
                false
            ),
            interaction,
            MaterialAlertDialogBuilder(activity)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CustomEntryViewHolder -> holder.bind(customEntries[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<String>) {
        customEntries = list
        notifyDataSetChanged()
    }

    fun addEntry() {
        notifyItemInserted(0)
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
        private val alertDialogBuilder: MaterialAlertDialogBuilder
    ): RecyclerView.ViewHolder(itemView) {
        private val customEntryHeader: TextView = itemView.findViewById(R.id.custom_entry_item_header)
        private val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(customEntry: String) {
            customEntryHeader.text = customEntry

            itemView.setOnClickListener {
                interaction.onItemTouch(customEntry, adapterPosition)
            }

            deleteButton.setOnClickListener {
                alertDialogBuilder
                    .setTitle("Are you sure you want to delete?")
                    .setMessage(customEntry)
                    .setPositiveButton("DELETE") { _, _ ->
                        interaction.onItemDelete(customEntry, adapterPosition)
                    }.setNegativeButton("CANCEL", null).create().show()
            }
        }
    }

    interface Interaction {
        fun onItemTouch(entry: String, position: Int)
        fun onItemDelete(entry: String, position: Int)
    }
}