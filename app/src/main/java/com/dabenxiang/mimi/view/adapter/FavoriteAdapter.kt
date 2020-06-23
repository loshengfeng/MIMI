package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_NORMAL
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_SHORT_VIDEO
import com.dabenxiang.mimi.view.favroite.FavoritePlayViewHolder
import com.dabenxiang.mimi.view.favroite.FavoritePostViewHolder

class FavoriteAdapter(
    private val listener: EventListener
) : PagedListAdapter<Any, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem
        }
    }

    enum class FunctionType { Video, Like, Favorite, Msg, Share, More }

    interface EventListener {
        fun onVideoClick(item: Any)
        fun onFunctionClick(type: FunctionType, view: View, item: Any)
        fun onAvatarDownload(view: ImageView, id: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NORMAL -> FavoritePlayViewHolder(layoutInflater.inflate(R.layout.item_favorite_normal, parent, false), listener)
            TYPE_SHORT_VIDEO -> FavoritePostViewHolder(layoutInflater.inflate(R.layout.item_favorite_short_video, parent, false), listener)
            else -> FavoritePlayViewHolder(layoutInflater.inflate(R.layout.item_favorite_normal, parent, false), listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FavoritePlayViewHolder -> holder.bind(item as PlayItem)
            is FavoritePostViewHolder -> holder.bind(item as PostFavoriteItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlayItem -> TYPE_NORMAL
            else -> TYPE_SHORT_VIDEO
        }
    }
}