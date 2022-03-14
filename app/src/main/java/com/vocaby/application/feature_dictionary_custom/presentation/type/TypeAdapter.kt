package com.vocaby.application.feature_dictionary_custom.presentation.type

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vocaby.application.R
import com.vocaby.application.core.util.DragStartListener
import com.vocaby.application.core.util.ItemTouchHelperAdapter
import com.vocaby.application.core.util.ItemTouchHelperViewHolder
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import java.util.*

class TypeAdapter(
    private var ctx: Context,
    private var dragStartListener: DragStartListener,
    private var itemInteractionListener: ItemInteractionListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {
    private var types: List<Type> = ArrayList()

    private val diffCallback = object : DiffUtil.ItemCallback<Type>() {
        override fun areItemsTheSame(
            oldItem: Type,
            newItem: Type
        ): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(
            oldItem: Type,
            newItem: Type
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    interface ItemInteractionListener {
        fun onItemRemoved(position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTypes: List<Type>) {
        types = newTypes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TypeAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_type,
                parent,
                false
            ),
            ctx,
            dragStartListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TypeAdapterViewHolder -> {
                holder.bind(types[position])
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                types[i].order = i + 1
                types[i + 1].order = i
                Collections.swap(types, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                types[i].order = i - 1
                types[i - 1].order = i
                Collections.swap(types, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun addItem() {
        notifyItemInserted(0)
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    override fun onItemDismiss(position: Int) {
        itemInteractionListener.onItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return types.size
    }

    @SuppressLint("ClickableViewAccessibility")
    class TypeAdapterViewHolder
    constructor(
        itemView: View,
        private val ctx: Context,
        private val dragStartListener: DragStartListener,
    ) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {
        private val card: MaterialCardView = itemView.findViewById(R.id.type_card)
        private val typeHeader: TextView = itemView.findViewById(R.id.type_card_header)
        private val dragHandle: FrameLayout = itemView.findViewById(R.id.drag_handle)

        fun bind(typeModel: Type) {
            typeHeader.text = typeModel.type

            dragHandle.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onDragStart(this)
                }

                false
            }
        }

        override fun onItemDragged() {
            card.strokeColor = ctx.getColor(R.color.colorPrimary)
            card.alpha = 0.8f
        }

        override fun onItemSwiped() {
            card.strokeColor = ctx.getColor(R.color.colorHeadline)
        }

        override fun onItemDone() {
            card.strokeColor = ctx.getColor(R.color.light_gray)
            card.elevation = 0f
            card.alpha = 1f
        }
    }
}