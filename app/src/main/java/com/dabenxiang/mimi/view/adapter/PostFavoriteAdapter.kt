package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PlayListItem
import com.dabenxiang.mimi.view.postfavorite.PostFavoriteFragment.Companion.TYPE_ADULT
import com.dabenxiang.mimi.view.postfavorite.PostFavoriteFragment.Companion.TYPE_NORMAL
import com.dabenxiang.mimi.view.postfavorite.PostFavoriteViewHolder

class PostFavoriteAdapter(
    private val listener: EventListener
) : PagedListAdapter<PlayListItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PlayListItem>() {
            override fun areItemsTheSame(
                oldItem: PlayListItem,
                newItem: PlayListItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PlayListItem,
                newItem: PlayListItem
            ): Boolean = oldItem == newItem
        }
    }

    interface EventListener {
        fun onVideoClick(view: View, item: PlayListItem)
        fun onLikeClick(view: View, item: PlayListItem)
        fun onFavoriteClick(view: View, item: PlayListItem)
        fun onMsgClick(view: View, item: PlayListItem)
        fun onShareClick(view: View, item: PlayListItem)
        fun onMoreClick(view: View, item: PlayListItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NORMAL -> PostFavoriteViewHolder(layoutInflater.inflate(R.layout.item_favorite_normal, parent, false), listener)
            TYPE_ADULT -> PostFavoriteViewHolder(layoutInflater.inflate(R.layout.item_favorite_adult, parent, false), listener)
            else -> PostFavoriteViewHolder(layoutInflater.inflate(R.layout.item_favorite_normal, parent, false), listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PostFavoriteViewHolder -> holder.bind(item)
//            is PostFavoriteViewHolder -> holder.bind(item)
        }
    }
}