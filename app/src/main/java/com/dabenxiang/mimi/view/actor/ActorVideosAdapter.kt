package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.view.actor.ActorFragment.Companion.VIEW_TYPE_ACTOR_VIDEOS
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.view.VideosSpaceItemDecoration

class ActorVideosAdapter(
    val context: Context,
    private val actorVideosFuncItem: ActorVideosFuncItem
) : RecyclerView.Adapter<ActorVideosViewHolder>() {
    private var selectItem: ActorVideosItem? = null

    private var actorVideosItems: ArrayList<ActorVideosItem>? = null

    private val actorVideoAdapter by lazy {
        ActorVideoAdapter(context,
            ActorVideoFuncItem(
                onVideoClickListener = { actorVideoItem, position -> actorVideosFuncItem.onVideoClickListener(actorVideoItem, position) }
            )
        ) }

    private val decorationPosition: ArrayList<Int> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ACTOR_VIDEOS
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
        val item = actorVideosItems?.get(position)?: ActorVideosItem()
        holder.name.run {
            text = item.name
            setOnClickListener { actorVideosFuncItem.onActorClickListener(item.id, position) }
        }
        holder.totalClick.text = item.totalClick?.toString() + context.getString(R.string.actor_hot_unit)
        holder.totalVideo.run {
            text = item.totalVideo?.toString() + context.getString(R.string.actor_videos_unit)
            setOnClickListener { actorVideosFuncItem.onActorClickListener(item.id, position) }
        }
        holder.actressesVideos.also{
            it.adapter = actorVideoAdapter
            if(!decorationPosition.contains(position)){
                it.addItemDecoration(
                    VideosSpaceItemDecoration(
                        GeneralUtils.dpToPx(context, 10),
                    )
                )
                decorationPosition.add(position)
            }
        }
        actorVideoAdapter.setupData(item.videos)
        actorVideosFuncItem.getActorAvatarAttachment.invoke(item?.attachmentId,holder.ivAvatar)
        holder.ivAvatar.setOnClickListener { actorVideosFuncItem.onActorClickListener(item.id, position) }
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