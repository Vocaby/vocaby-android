package com.vocaby.app.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.app.R
import com.vocaby.app.adapters.TypeAdapter.TypeViewHolder
import java.util.*

class TypeAdapter(private val itemInteractionListener: ItemInteractionListener) :
    RecyclerView.Adapter<TypeViewHolder>() {
    private var types: MutableList<String> = ArrayList()
    private var lastCheckedPosition = -1

    interface ItemInteractionListener {
        fun onTypeClicked(type: String)
    }

    fun addItem(type: String) {
        types.add(0, type)
        notifyItemInserted(0)
        resetSelect()
    }

    fun removeItem(type: String) {
        val position = types.indexOf(type)
        if (position != -1) {
            types.removeAt(position)
            notifyItemRemoved(position)
        }
        resetSelect()
    }

    private fun resetSelect() {
        lastCheckedPosition = -1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(newList: MutableList<String>) {
        types = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.type_item, parent, false)
        return TypeViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
        holder.typeHeader.text = types[holder.adapterPosition]
        holder.cardView.setOnClickListener {
            lastCheckedPosition = holder.adapterPosition
            itemInteractionListener.onTypeClicked(types[lastCheckedPosition])
            notifyDataSetChanged()
        }
        holder.cardView.isSelected = position == lastCheckedPosition
    }

    override fun getItemCount(): Int {
        return types.size
    }

    class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var typeHeader: TextView = itemView.findViewById(R.id.type_header)
        var cardView: CardView = itemView.findViewById(R.id.card_container)

    }
}