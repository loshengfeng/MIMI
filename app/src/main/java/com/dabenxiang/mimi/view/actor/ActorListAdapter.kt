package com.dabenxiang.mimi.view.actor

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.callback.MyPostListener
import com.dabenxiang.mimi.model.api.vo.ActorCategoriesItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.viewHolder.*
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils

class ActorListAdapter(
        val context: Context,
        private val actorCategoriesFuncItem: ActorCategoriesFuncItem = ActorCategoriesFuncItem()
) : PagingDataAdapter<ActorCategoriesItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private const val VIEW_TYPE_NORMAL = 0

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

    var holder: ActorCategoriesViewHolder? = null

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> {
               holder = ActorCategoriesViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_actor_categories, parent, false)
                )
                holder!!
            }
            else -> {
                ActorCategoriesViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_actor_categories, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {
            when (holder) {
                is ActorCategoriesViewHolder -> {
                    holder.onBind(
                        it,
                        actorCategoriesFuncItem,
                        position
                    )
                }
            }
        }
    }

    fun getHolderParameter(): GridLayoutManager.LayoutParams {
        val params = holder?.clCategory?.layoutParams
        return if(params != null)
            params as GridLayoutManager.LayoutParams
        else
            GridLayoutManager.LayoutParams(70,117)
    }

}