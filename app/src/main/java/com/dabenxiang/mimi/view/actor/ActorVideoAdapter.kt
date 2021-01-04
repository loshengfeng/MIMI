package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorVideoItem

class ActorVideoAdapter(
    val context: Context,
    val actorVideoFuncItem: ActorVideoFuncItem
) : RecyclerView.Adapter<ActorVideoViewHolder>() {

    private var selectItem: ActorVideoItem? = null

    private var actorVideoItems: ArrayList<ActorVideoItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorVideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor_video, parent, false)
        return ActorVideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return actorVideoItems?.size ?: 0
    }

    override fun onBindViewHolder(holder: ActorVideoViewHolder, position: Int) {
        val item = actorVideoItems?.get(position)
        holder.title.text = item?.title
        Glide.with(context).load(item?.cover).into(holder.image)
        if(item != null)
            holder.item.setOnClickListener { actorVideoFuncItem.onVideoClickListener(item, position) }
    }

    fun submitList(data: ArrayList<ActorVideoItem>) {
        actorVideoItems = data
        notifyDataSetChanged()
    }

    fun clearSelectItem() {
        selectItem = null
    }

    fun getSelectItem(): ActorVideoItem? {
        return selectItem
    }
}