package com.vocaby.application.feature_user.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R

import com.vocaby.application.feature_user.domain.model.FaqModel
import java.util.*

class FaqAdapter: RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {
    private var faqList: List<FaqModel> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setList(faq: List<FaqModel>) {
        faqList = faq
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        return FaqViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.faq_item, parent, false))
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(faqList[position])
    }

    override fun getItemCount(): Int = faqList.size

    class FaqViewHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val header = itemView.findViewById<TextView>(R.id.faq_header)
        private val content = itemView.findViewById<TextView>(R.id.faq_content)
        fun bind(faq: FaqModel) {
            val headerText = "Q${adapterPosition+1}. ${faq.header}"
            header.text = headerText
            content.text = faq.content
        }
    }
}