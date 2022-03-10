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

class PrevPickAdapter(
    private val ctx: Context,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PrevPickAdapterViewHolder(
            ctx,
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_prev_pick,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PrevPickAdapterViewHolder -> {
                val isRandom = (itemCount == 1 && position == 0) || (itemCount == 2 && position == 1)
                holder.bind(differ.currentList[position], isRandom)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<String>) {
        differ.submitList(list)
    }

    class PrevPickAdapterViewHolder
    constructor(
        private val ctx: Context,
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val entryView = itemView.findViewById<TextView>(R.id.entry)
        private val pickTag = itemView.findViewById<TextView>(R.id.pick_tag)
        fun bind(pick: String, isRandom: Boolean) {
            entryView.text = pick

            if (isRandom) {
                pickTag.setText(R.string.wod_random_pick)
                pickTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                pickTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadlineSoft))
            } else {
                pickTag.setText(R.string.wod_our_pick)
                pickTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimaryAccent))
                pickTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorSecondary))
            }
        }
    }
}