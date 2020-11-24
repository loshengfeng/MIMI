package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem

class ActorVideosAdapter(
    val context: Context,
    val actorVideosFuncItem: ActorVideosFuncItem
) : RecyclerView.Adapter<ActorVideosViewHolder>() {

    private var selectItem: ActorVideosItem? = null

    private var actorVideosItems: ArrayList<ActorVideosItem>? = null

    interface EventListener {
        fun onClickListener(item: ActorVideosItem, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorVideosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor_videos, parent, false)
        return ActorVideosViewHolder(view)
    }

    override fun getItemCount(): Int {
        return actorVideosItems?.size ?: 0
    }

    override fun onBindViewHolder(holder: ActorVideosViewHolder, position: Int) {
        val item = actorVideosItems?.get(position)
        holder.name.text = item?.name
        holder.totalClick.text = item?.totalClick?.toString() + context.getString(R.string.actor_hot_unit)
        holder.totalVideo.text = item?.totalVideo?.toString() + context.getString(R.string.actor_videos_unit)
        actorVideosFuncItem.getActorAvatarAttachment.invoke(item?.attachmentId,holder.ivAvatar)
    }

    fun setupData(data: ArrayList<ActorVideosItem>) {
        actorVideosItems = data
    }

    fun clearSelectItem() {
        selectItem = null
    }

    fun getSelectItem(): ActorVideosItem? {
        return selectItem
    }
}