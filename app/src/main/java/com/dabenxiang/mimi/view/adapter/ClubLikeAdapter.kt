package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MyLikeListener
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.adapter.viewHolder.ClubLikeViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.DeletedItemViewHolder
import timber.log.Timber

class ClubLikeAdapter(
    val listener: MyLikeListener
) : PagingDataAdapter<PostFavoriteItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PostFavoriteItem>() {
            override fun areItemsTheSame(
                oldItem: PostFavoriteItem,
                newItem: PostFavoriteItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PostFavoriteItem,
                newItem: PostFavoriteItem
            ): Boolean = oldItem == newItem
        }
    }

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        return if (removedPosList.contains(position)) {
            R.layout.item_deleted
        } else {
            R.layout.item_clip_post
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Timber.e("Show  ClubLikeAdapter onCreateViewHolder " + itemCount)
        val itemview = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_clip_post -> ClubLikeViewHolder(itemview)
            else -> DeletedItemViewHolder(itemview)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.also {
            when (holder) {
                is ClubLikeViewHolder -> {
                    holder.onBind(
                        it,
                        null,
                        position,
                        listener
                    )
                }
            }
        }
    }
}