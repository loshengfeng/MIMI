package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class ActorCategoriesAdapter(
    val context: Context,
    val actorCategoriesFuncItem: ActorCategoriesFuncItem
) : RecyclerView.Adapter<ActorCategoriesViewHolder>() {

    private var selectItem: ActorCategoriesItem? = null

    private var actorCategoriesItems: ArrayList<ActorCategoriesItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorCategoriesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor_categories, parent, false)
        return ActorCategoriesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return actorCategoriesItems?.size ?: 0
    }

    override fun onBindViewHolder(holder: ActorCategoriesViewHolder, position: Int) {
        val item = actorCategoriesItems?.get(position)
        holder.name.text = item?.name
        actorCategoriesFuncItem.getActorAvatarAttachment.invoke(item?.attachmentId,holder.ivAvatar)
        if(item != null)
            holder.item.setOnClickListener { actorCategoriesFuncItem.onClickListener(item, position) }
    }

    fun setupData(data: ArrayList<ActorCategoriesItem>) {
        actorCategoriesItems = data
    }

    fun clearSelectItem() {
        selectItem = null
    }

    fun getSelectItem(): ActorCategoriesItem? {
        return selectItem
    }
}