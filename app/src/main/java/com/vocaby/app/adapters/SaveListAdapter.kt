package com.vocaby.app.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.app.R
import kotlinx.android.synthetic.main.save_item.view.*

class SaveListAdapter(private val activity: Activity, private val interaction: Interaction) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SavesAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.save_item,
                parent,
                false
            ),
            interaction,
            MaterialAlertDialogBuilder(activity)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SavesAdapterViewHolder -> {
                holder.bind(differ.currentList.get(position))
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
        private val alertDialogBuilder: MaterialAlertDialogBuilder
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(entry: String) = with(itemView) {
            itemView.save_item.text = entry

            itemView.setOnClickListener {
                interaction.onItemTouch(entry)
            }

            itemView.unsave_button.setOnClickListener {
                alertDialogBuilder
                    .setTitle("Are you sure you want to delete?")
                    .setMessage(entry)
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