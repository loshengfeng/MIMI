package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.view.adapter.viewHolder.DeletedItemViewHolder
import com.dabenxiang.mimi.view.adapter.viewHolder.MiMiLikeViewHolder

class MiMiLikeAdapter(
    private val listener: EventListener
) : PagingDataAdapter<PostFavoriteItem, RecyclerView.ViewHolder>(diffCallback) {
    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_DELETED = 1

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

    interface EventListener {
        fun onDetail(item: PostFavoriteItem)
        fun onGetAttachment(id: Long, view: ImageView)
        fun onCancelFollow(userId: Long, position: Int)
    }

    var removedPosList = ArrayList<Int>()

    override fun getItemViewType(position: Int): Int {
        return if (removedPosList.contains(position)) {
            VIEW_TYPE_DELETED
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> MiMiLikeViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_clip_post, parent, false),
                listener
            )
            else -> DeletedItemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_deleted, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is MiMiLikeViewHolder -> holder.bind(item, position)
        }
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}