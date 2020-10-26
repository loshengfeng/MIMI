package com.dabenxiang.mimi.view.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.VideoViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.nested_item_home_statistics.view.*

class GuessLikeAdapter(
    private val listener: GuessLikeAdapterListener,
    private val isAdult: Boolean
) :
    PagedListAdapter<BaseVideoItem, BaseViewHolder>(diffCallback) {

    interface GuessLikeAdapterListener {
        fun onVideoClick(view: View, item: PlayerItem)
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
                        val playerData = PlayerItem.parser(it)
                        listener.onVideoClick(view, playerData)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_statistics, parent, false)
        val layoutParams = view.layout_card.layoutParams as ConstraintLayout.LayoutParams
        if(isAdult) {
            layoutParams.width = (((200.0 / 360.0)) * GeneralUtils.getWindowsWidth()).toInt()
            layoutParams.height = (((113.0 / 640.0)) * GeneralUtils.getWindowsHeight()).toInt()
        } else {
            layoutParams.width = (((100.0 / 360.0)) * GeneralUtils.getWindowsWidth()).toInt()
            layoutParams.height = (((144.0 / 640.0)) * GeneralUtils.getWindowsHeight()).toInt()
        }
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