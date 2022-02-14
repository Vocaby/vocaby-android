package com.vocaby.vocabywidgets.searchview.suggestions

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.vocabywidgets.R
import com.vocaby.vocabywidgets.searchview.suggestions.model.SearchSuggestion

class SearchSuggestionsAdapter(
    private val mListener: Listener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataSet: List<SearchSuggestion> = ArrayList()

    interface Listener {
        fun onItemSelected(item: SearchSuggestion)
        fun onMoveItemToSearchClicked(item: SearchSuggestion)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(searchSuggestions: List<SearchSuggestion>) {
        dataSet = searchSuggestions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_view_suggestion_item, viewGroup, false)

        return SearchSuggestionViewHolder(
            view,
            object : SearchSuggestionViewHolder.Listener {
                override fun onItemClicked(adapterPosition: Int) {
                    mListener?.onItemSelected(dataSet[adapterPosition])
                }

                override fun onMoveItemToSearchClicked(adapterPosition: Int) {
                    mListener?.onMoveItemToSearchClicked(
                        dataSet[adapterPosition]
                    )
                }
            }
        )
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = vh as SearchSuggestionViewHolder
        viewHolder.bind(dataSet)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class SearchSuggestionViewHolder(
        v: View,
        private val mListener: Listener?
    ): RecyclerView.ViewHolder(v) {
        var body: TextView = v.findViewById<View>(R.id.body) as TextView
        var leftIcon: ImageView = v.findViewById<View>(R.id.left_icon) as ImageView
        var rightIcon: ImageView = v.findViewById<View>(R.id.right_icon) as ImageView

        interface Listener {
            fun onItemClicked(adapterPosition: Int)
            fun onMoveItemToSearchClicked(adapterPosition: Int)
        }

        init {
            rightIcon.setOnClickListener {
                val adapterPosition = adapterPosition
                if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    mListener.onMoveItemToSearchClicked(getAdapterPosition())
                }
            }

            itemView.setOnClickListener {
                val adapterPosition = adapterPosition
                if (mListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    mListener.onItemClicked(adapterPosition)
                }
            }
        }

        fun bind(suggestionsItems: List<SearchSuggestion>) {
            body.text = suggestionsItems[adapterPosition].body
        }
    }
}