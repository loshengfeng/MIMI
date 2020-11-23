package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem

class ActorCategoriesAdapter(
    val context: Context,
    val listener: EventListener
) : RecyclerView.Adapter<ActorCategoriesViewHolder>() {

    private var selectItem: ActorCategoriesItem? = null

    private var actorCategoriesItems: ArrayList<ActorCategoriesItem>? = null

    interface EventListener {
        fun onClickListener(item: ActorCategoriesItem, position: Int)
    }

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