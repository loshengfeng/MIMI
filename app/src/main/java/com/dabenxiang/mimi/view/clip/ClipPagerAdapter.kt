package com.dabenxiang.mimi.view.clip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R

class ClipPagerAdapter(private val clipFuncItem: ClipFuncItem) : RecyclerView.Adapter<ClipPagerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ClipPagerViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_clip_pager, parent, false)
    )

    override fun onBindViewHolder(holder: ClipPagerViewHolder, position: Int) =
        holder.onBind(position, clipFuncItem)

    override fun getItemCount() = 2
}