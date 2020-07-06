package com.dabenxiang.mimi.view.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.VideoViewHolder

class GuessLikeAdapter(
    private val listener: GuessLikeAdapterListener,
    private val isAdult: Boolean
) :
    PagedListAdapter<BaseVideoItem, BaseViewHolder>(diffCallback) {

    interface GuessLikeAdapterListener {
        fun onVideoClick(view: View, item: PlayerData)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<BaseVideoItem>() {
            override fun areItemsTheSame(oldItem: BaseVideoItem, newItem: BaseVideoItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: BaseVideoItem,
                newItem: BaseVideoItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val videoViewHolderListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                if (index > -1) {
                    getItem(index)?.also {
                        val playerData = PlayerData.parser(it, isAdult)
                        listener.onVideoClick(view, playerData)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_categories, parent, false)
        return VideoViewHolder(
            view,
            videoViewHolderListener
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.also {
            (holder as VideoViewHolder).bind(it as BaseVideoItem.Video, position)
        }
    }
}