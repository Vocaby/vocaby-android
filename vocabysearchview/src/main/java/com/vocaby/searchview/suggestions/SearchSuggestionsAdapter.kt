package com.vocaby.searchview.suggestions

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import android.widget.TextView
import com.vocaby.searchview.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import com.vocaby.searchview.suggestions.model.SearchSuggestion
import com.vocaby.searchview.util.Util
import java.util.*

/**
 * Copyright (C) 2015 Ari C.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class SearchSuggestionsAdapter(
    mContext: Context,
    private val mBodyTextSizePx: Int,
    private val mListener: Listener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataSet: List<SearchSuggestion> = ArrayList()
    private val mRightIconDrawable: Drawable = Util.getWrappedDrawable(mContext, R.drawable.ic_arrow_back_black_24dp)
    private var mShowRightMoveUpBtn = false
    private var mTextColor = -1
    private var mRightIconColor = -1

    interface OnBindSuggestionCallback {
        fun onBindSuggestion(
            suggestionView: View?, leftIcon: ImageView?, textView: TextView?,
            item: SearchSuggestion?, itemPosition: Int
        )
    }

    private var mOnBindSuggestionCallback: OnBindSuggestionCallback? = null

    interface Listener {
        fun onItemSelected(item: SearchSuggestion)
        fun onMoveItemToSearchClicked(item: SearchSuggestion)
    }

    class SearchSuggestionViewHolder(v: View, private val mListener: Listener?) :
        RecyclerView.ViewHolder(v) {
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
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(searchSuggestions: List<SearchSuggestion>) {
        dataSet = searchSuggestions
        notifyDataSetChanged()
    }

    fun setOnBindSuggestionCallback(callback: OnBindSuggestionCallback?) {
        mOnBindSuggestionCallback = callback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.search_suggestion_item, viewGroup, false)
        val viewHolder = SearchSuggestionViewHolder(view,
            object : SearchSuggestionViewHolder.Listener {
                override fun onItemClicked(adapterPosition: Int) {
                    mListener?.onItemSelected(dataSet[adapterPosition])
                }

                override fun onMoveItemToSearchClicked(adapterPosition: Int) {
                    mListener?.onMoveItemToSearchClicked(
                        dataSet[adapterPosition]
                    )
                }
            })
        viewHolder.rightIcon.setImageDrawable(mRightIconDrawable)
        return viewHolder
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = vh as SearchSuggestionViewHolder
        if (!mShowRightMoveUpBtn) {
            viewHolder.rightIcon.isEnabled = false
            viewHolder.rightIcon.visibility = View.INVISIBLE
        } else {
            viewHolder.rightIcon.isEnabled = true
            viewHolder.rightIcon.visibility = View.VISIBLE
        }
        val suggestionItem = dataSet[position]
        viewHolder.body.text = suggestionItem.body
        if (mTextColor != -1) {
            viewHolder.body.setTextColor(mTextColor)
        }
        if (mRightIconColor != -1) {
            Util.setIconColor(viewHolder.rightIcon, mRightIconColor)
        }
        if (mOnBindSuggestionCallback != null) {
            mOnBindSuggestionCallback!!.onBindSuggestion(
                viewHolder.itemView, viewHolder.leftIcon, viewHolder.body,
                suggestionItem, position
            )
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTextColor(color: Int) {
        var notify = false
        if (mTextColor != color) {
            notify = true
        }
        mTextColor = color
        if (notify) {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRightIconColor(color: Int) {
        var notify = false
        if (mRightIconColor != color) {
            notify = true
        }
        mRightIconColor = color
        if (notify) {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setShowMoveUpIcon(show: Boolean) {
        var notify = false
        if (mShowRightMoveUpBtn != show) {
            notify = true
        }
        mShowRightMoveUpBtn = show
        if (notify) {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reverseList() {
        dataSet = dataSet.reversed()
        notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "SearchSuggestionsAdapter"
    }

    init {
        DrawableCompat.setTint(
            mRightIconDrawable,
            Util.getColor(mContext, R.color.gray_active_icon)
        )
    }
}