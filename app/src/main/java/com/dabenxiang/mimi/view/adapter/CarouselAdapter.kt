package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseViewHolder

class CarouselAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    override fun getItemCount(): Int {
        return 5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BaseViewHolder(layoutInflater.inflate(R.layout.item_item_carousel, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

    }
}