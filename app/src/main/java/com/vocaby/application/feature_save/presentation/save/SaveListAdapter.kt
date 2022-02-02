package com.vocaby.application.feature_save.presentation.save

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.application.R

class SaveListAdapter(activity: Activity, private val interaction: Interaction, private val isCollection: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    private val materialAlertDialogBuilder = MaterialAlertDialogBuilder(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SavesAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_save,
                parent,
                false
            ),
            interaction,
            materialAlertDialogBuilder,
            isCollection
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SavesAdapterViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<String>) {
        differ.submitList(list)
    }

    class SavesAdapterViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction,
        private val alertDialogBuilder: MaterialAlertDialogBuilder,
        private val isCollection: Boolean,
    ) : RecyclerView.ViewHolder(itemView) {
        private val saveItem: TextView = itemView.findViewById(R.id.save_item)
        private val unsaveButton: AppCompatImageButton = itemView.findViewById(R.id.unsave_button)
        fun bind(entry: String) {
            saveItem.text = entry

            itemView.setOnClickListener {
                interaction.onItemTouch(entry)
            }

            unsaveButton.setOnClickListener {
                val message = if (isCollection) {
                    entry
                } else {
                    "Removing this save will also remove it from all collections\n\n$entry\n"
                }

                alertDialogBuilder
                    .setTitle("Are you sure you want to delete?")
                    .setMessage(message)
                    .setPositiveButton("DELETE") { _, _ ->
                        interaction.onItemDelete(entry)
                    }.setNegativeButton("CANCEL", null).create().show()
            }
        }
    }

    interface Interaction {
        fun onItemTouch(entry: String)
        fun onItemDelete(entry: String)
    }
}