package com.vocaby.app.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vocaby.app.R
import com.vocaby.app.adapters.CustomGroupAdapter.CustomGroupViewHolder
import com.vocaby.app.models.dictionary.DefinitionGroupModel
import java.util.*

class CustomGroupAdapter(
    var ctx: Context,
    private var dragStartListener: DragStartListener,
    private var itemInteractionListener: ItemInteractionListener
) : RecyclerView.Adapter<CustomGroupViewHolder>(), ItemTouchHelperAdapter {
    private var groups: List<DefinitionGroupModel>

    interface ItemInteractionListener {
        fun onGroupCardClicked(position: Int)
        fun onItemRemoved(position: Int)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(newList: List<DefinitionGroupModel>) {
        groups = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomGroupViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.custom_group_row, parent, false)
        return CustomGroupViewHolder(view, ctx)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: CustomGroupViewHolder, position: Int) {
        holder.bind(groups[holder.adapterPosition].type, groups[holder.adapterPosition].definitionData.size)

        holder.dragHandle.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                dragStartListener.onDragStart(holder)
            }
            false
        }

        holder.container.setOnClickListener {
            itemInteractionListener.onGroupCardClicked(
                holder.adapterPosition
            )
        }
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                groups[i].order = i + 1
                groups[i + 1].order = i
                Collections.swap(groups, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                groups[i].order = i - 1
                groups[i - 1].order = i
                Collections.swap(groups, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        if (position != -1) itemInteractionListener.onItemRemoved(position)
    }

    fun addItem() {
        notifyItemInserted(groups.size - 1)
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    fun editItem(position: Int) {
        notifyItemChanged(position)
    }

    class CustomGroupViewHolder(itemView: View, val ctx: Context) : RecyclerView.ViewHolder(itemView),
        ItemTouchHelperViewHolder {
        private var groupHeader: TextView = itemView.findViewById(R.id.group_card_header)
        private var definitionCounter: TextView = itemView.findViewById(R.id.group_card_def_counter)
        private var definitionCounterHeader: TextView = itemView.findViewById(R.id.group_card_def_counter_header)
        var dragHandle: FrameLayout = itemView.findViewById(R.id.drag_handle)
        var container: View = itemView.findViewById(R.id.card_container)

        fun bind(header: String, count: Int) {
            groupHeader.text = header
            definitionCounter.text = "$count"

            if (count > 1) {
                definitionCounterHeader.setText(R.string.definition_header_plural)
            } else {
                definitionCounterHeader.setText(R.string.definition_header_singular)
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

    init {
        groups = ArrayList()
    }
}