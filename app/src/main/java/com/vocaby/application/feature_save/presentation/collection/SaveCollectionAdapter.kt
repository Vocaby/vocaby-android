package com.vocaby.application.feature_save.presentation.collection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel

class SaveCollectionAdapter(
    private val interaction: Interaction
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val diffCallback = object : DiffUtil.ItemCallback<SaveCollectionModel>() {
        override fun areItemsTheSame(
            oldItem: SaveCollectionModel,
            newItem: SaveCollectionModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: SaveCollectionModel,
            newItem: SaveCollectionModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SaveCollectionAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_save_collection,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SaveCollectionAdapterViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<SaveCollectionModel>) {
        differ.submitList(list)
    }

    class SaveCollectionAdapterViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction
    ) : RecyclerView.ViewHolder(itemView) {
        private val collectionHeader: TextView = itemView.findViewById(R.id.collection_header)
        private val collectionCounter: TextView = itemView.findViewById(R.id.collection_counter)
        private val cardView: CardView = itemView.findViewById(R.id.card_container)
        private val moreButton: AppCompatImageButton = itemView.findViewById(R.id.more_button)

        fun bind(collectionModel: SaveCollectionModel) {
            collectionHeader.text = collectionModel.collectionName
            val countText = "${Formatter.cleanNumber(collectionModel.count)} Saved"
            collectionCounter.text = countText

            cardView.setOnClickListener {
                interaction.onItemTouch(collectionModel.collectionName, collectionModel.id)
            }

            moreButton.setOnClickListener {
                interaction.onItemUpdate(collectionModel.collectionName, collectionModel.id)
            }
        }
    }

    interface Interaction {
        fun onItemTouch(name: String, id: Int)
        fun onItemUpdate(collectionName: String, collectionId: Int)
    }
}