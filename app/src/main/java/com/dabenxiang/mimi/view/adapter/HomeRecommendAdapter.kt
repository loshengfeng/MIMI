package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.nested_item_home_recommend.view.*

class HomeRecommendAdapter : RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nested_item_home_recommend, parent, false)
        return BaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.itemView.tv_title.text = "標題${position + 1}標題標題標題標題標題標題標題標題標題標題標題標題"
    }
}