package com.vocaby.application.feature_dictionary_custom.presentation.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vocaby.application.R
import com.vocaby.application.core.util.DragStartListener
import com.vocaby.application.core.util.ItemTouchHelperAdapter
import com.vocaby.application.core.util.ItemTouchHelperViewHolder
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary_custom.presentation.adapter.CustomDefAdapter.CustomDefViewHolder
import java.util.*

class CustomDefAdapter(
    private var ctx: Context,
    private var dragStartListener: DragStartListener,
    private var itemInteractionListener: ItemInteractionListener
) : RecyclerView.Adapter<CustomDefViewHolder>(), ItemTouchHelperAdapter {
    var definitions: List<DefinitionModel> = ArrayList()

    interface ItemInteractionListener {
        fun onItemRemoved(position: Int)
        fun onItemTouched(position: Int, definition: String, example: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomDefViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_definition_row, parent, false)
        return CustomDefViewHolder(view, dragStartListener, itemInteractionListener, ctx)
    }

    override fun onBindViewHolder(holder: CustomDefViewHolder, position: Int) {
        val example = definitions[position].example ?: ""
        holder.bind(definitions[position].definition, example)
    }

    override fun getItemCount(): Int {
        return definitions.size
    }

    fun setList(newList: List<DefinitionModel>) {
        // soft copy
        definitions = newList
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                definitions[i].order = i + 1
                definitions[i + 1].order = i
                Collections.swap(definitions, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                definitions[i].order = i - 1
                definitions[i - 1].order = i
                Collections.swap(definitions, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        itemInteractionListener.onItemRemoved(position)
    }

    fun addItem() {
        notifyItemInserted(itemCount - 1)
    }

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    class CustomDefViewHolder
    constructor(
        itemView: View,
        private val dragStartListener: DragStartListener,
        private val itemInteractionListener: ItemInteractionListener,
        private val ctx: Context
    ) : RecyclerView.ViewHolder(itemView),
        ItemTouchHelperViewHolder {
        private var definitionView: TextView = itemView.findViewById(R.id.definition)
        private var exampleView: TextView = itemView.findViewById(R.id.example)
        private var dragHandle: FrameLayout = itemView.findViewById(R.id.drag_handle)
        private var container: LinearLayout = itemView.findViewById(R.id.card_container)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(definition: String, example: String) {
            definitionView.text = definition
            if (example.isEmpty()) {
                exampleView.visibility = View.GONE
            } else {
                exampleView.visibility = View.VISIBLE
                exampleView.text = example
            }

            dragHandle.setOnTouchListener { _, motionEvent: MotionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onDragStart(this)
                }
                false
            }

            container.setOnClickListener {
                itemInteractionListener.onItemTouched(adapterPosition, definition, example)
            }
        }

        override fun onItemDragged() {
            (itemView as MaterialCardView).strokeColor = ctx.getColor(R.color.colorPrimary)
        }

        override fun onItemSwiped() {
            (itemView as MaterialCardView).strokeColor = ctx.getColor(R.color.colorHeadline)
        }

        override fun onItemDone() {
            (itemView as MaterialCardView).strokeColor = ctx.getColor(R.color.light_gray)
        }
    }
}