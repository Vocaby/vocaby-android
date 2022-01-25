package com.vocaby.application.feature_dictionary.presentation.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.feature_save.domain.model.AddCollectionModel
import java.util.*

class CollectionAdapter() :
    RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {
    private var collections: List<AddCollectionModel> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setList(newList: List<AddCollectionModel>) {
        collections = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_collection_item, parent, false)
        return CollectionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return collections.size
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.typeHeader.text = collections[position].name
        holder.cardView.setOnClickListener {
            collections[position].saved = !collections[position].saved
            notifyDataSetChanged()
        }

        holder.cardView.isSelected = collections[position].saved
    }

    class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var typeHeader: TextView = itemView.findViewById(R.id.collection_header)
        var cardView: CardView = itemView.findViewById(R.id.card_container)
    }
}