package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.model.vo.PlayerItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.GridAdHolder
import com.dabenxiang.mimi.view.home.viewholder.VideoViewHolder
import kotlinx.android.synthetic.main.layout_item_video.view.*

class CategoryVideoListAdapter(
    private val nestedListener: HomeAdapter.EventListener
) : PagedListAdapter<BaseVideoItem, BaseViewHolder>(diffCallback) {

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

        private const val AD = 0
        private const val VIDEO = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIDEO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.nested_item_video, parent, false)
                val layoutParams = view.layout_card.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.dimensionRatio = "155:87"
                view.layout_card.layoutParams = layoutParams
                VideoViewHolder(view, videoViewHolderListener)
            }
            else -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_ad, parent, false)
                GridAdHolder(view, videoViewHolderListener)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.also {
            when (it) {
                is BaseVideoItem.Video -> {
                    (holder as VideoViewHolder).bind(it, position)
                }
                is BaseVideoItem.Banner -> {
                    (holder as GridAdHolder).bind(it, position)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BaseVideoItem.Video -> VIDEO
            else -> AD
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (getItemViewType(position)) {
                        AD -> 2
                        else -> 1
                    }
                }
            }
    }

    private val videoViewHolderListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                if (index > -1) {
                    getItem(index)?.also {
                        val playerData = PlayerItem.parser(it)
                        nestedListener.onVideoClick(view, playerData)
                    }
                }
            }
        }
    }

}