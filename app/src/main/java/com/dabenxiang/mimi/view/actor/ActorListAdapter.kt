package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.view.actor.ActorFragment.Companion.VIEW_TYPE_ACTOR_LIST
import com.dabenxiang.mimi.view.base.BaseViewHolder

class ActorListAdapter(
        val context: Context,
        private val actorListFuncItem: ActorListFuncItem = ActorListFuncItem()
) : PagingDataAdapter<ActorCategoriesItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ActorCategoriesItem>() {
            override fun areItemsTheSame(
                    oldItem: ActorCategoriesItem,
                    newItem: ActorCategoriesItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                    oldItem: ActorCategoriesItem,
                    newItem: ActorCategoriesItem
            ): Boolean = oldItem == newItem
        }
    }

    var holder: ActorHeaderViewHolder? = null

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ACTOR_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ActorListViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_actor_categories, parent, false)
                )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {
            when (holder) {
                is ActorListViewHolder -> {
                    (holder as ActorListViewHolder).onBind(
                        it,
                        actorListFuncItem,
                        position
                    )
                }
            }
        }
    }

    fun isDataEmpty() = itemCount == 0
}