package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.VideoHolderItem
import com.dabenxiang.mimi.model.serializable.PlayerData
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.home.HomeTemplate
import com.dabenxiang.mimi.view.home.VideoViewHolder

class HomeVideoListAdapter(private val nestedListener: HomeAdapter.EventListener) : RecyclerView.Adapter<VideoViewHolder>() {

    private var data: List<VideoHolderItem>? = null

    fun setDataSrc(src: HomeTemplate.VideoList) {
        data = src.videoList

        updated()
    }

    private fun updated() {

        notifyDataSetChanged()
    }

    private val videoViewHolderListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                if (index > -1) {
                    data?.get(index)?.also {
                        nestedListener.onVideoClick(view, PlayerData.parser(it))
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position % 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val layout = when (viewType) {
            0 -> R.layout.nested_item_left_video
            else -> R.layout.nested_item_right_video
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return VideoViewHolder(view, videoViewHolderListener)
    }

    override fun getItemCount(): Int {
        return data?.count() ?: 0
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        data?.also { it ->
            holder.bind(it[position], position)
        }
    }
}