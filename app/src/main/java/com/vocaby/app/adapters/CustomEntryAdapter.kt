package com.vocaby.app.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vocaby.app.R
import com.vocaby.app.models.customentry.UserEntry
import com.vocaby.app.utils.Formatter

class CustomEntryAdapter(
    private val activity: Activity,
    private val interaction: Interaction
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var customEntries: List<UserEntry> = ArrayList()
    private val alertDialogBuilder = MaterialAlertDialogBuilder(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomEntryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.custom_entry_item,
                parent,
                false
            ),
            interaction,
            alertDialogBuilder,
            activity
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
        private val alertDialogBuilder: MaterialAlertDialogBuilder,
        private val activity: Activity
    ): RecyclerView.ViewHolder(itemView) {
        private val header: TextView = itemView.findViewById(R.id.custom_entry_item_header)
        private val lastUpdated: TextView = itemView.findViewById(R.id.custom_entry_item_updated)
        private val moreButton: ImageButton = itemView.findViewById(R.id.more_button)
        @SuppressLint("RestrictedApi")
        val listPopupWindow = ListPopupWindow(
            activity
        ).apply {
            setOverlapAnchor(true)
            width = 300
            anchorView = moreButton
            setDropDownGravity(Gravity.END)
            val items = listOf("Edit Entry", "Delete Entry")
            val adapter: ArrayAdapter<String> = object: ArrayAdapter<String>(activity.applicationContext, R.layout.list_popup_window_item, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    if (position == 1 ) {
                        view.setTextColor(activity.getColor(R.color.colorHeadline))
                    } else {
                        view.setTextColor(activity.getColor(R.color.black))
                    }

                    return view
                }
            }

            setAdapter(adapter)
            setBackgroundDrawable(ResourcesCompat.getDrawable(activity.resources, R.drawable.box_white, null))
        }

        fun bind(userEntry: UserEntry) {
            header.text = userEntry.entry
            lastUpdated.text = Formatter.formatDateStringForDisplay(userEntry.lastUpdated)

            itemView.setOnClickListener {
                interaction.onItemTouch(userEntry.entry, adapterPosition)
            }

            moreButton.setOnClickListener {
                listPopupWindow.show()
            }

            listPopupWindow.setOnItemClickListener { _, _, position: Int, _: Long ->
                // Respond to list popup window item click.
                when(position) {
                    0 -> {
                        interaction.onItemTouch(userEntry.entry, adapterPosition)
                    }
                    1 -> {
                        alertDialogBuilder
                            .setTitle("Are you sure you want to delete?")
                            .setMessage(userEntry.entry)
                            .setPositiveButton("DELETE") { _, _ ->
                                interaction.onItemDelete(userEntry.entry, adapterPosition)
                            }.setNegativeButton("CANCEL", null).create().show()
                    }
                }

                // Dismiss popup.
                listPopupWindow.dismiss()
            }
        }
    }

    interface Interaction {
        fun onItemTouch(entry: String, position: Int)
        fun onItemDelete(entry: String, position: Int)
    }
}