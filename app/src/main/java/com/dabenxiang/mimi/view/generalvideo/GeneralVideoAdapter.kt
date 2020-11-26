package com.dabenxiang.mimi.view.generalvideo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.view.adapter.viewHolder.AdHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.item_general_video.view.*

class GeneralVideoAdapter(
    val onItemClick: (StatisticsItem) -> Unit
) : PagingDataAdapter<StatisticsItem, RecyclerView.ViewHolder>(COMPARATOR) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<StatisticsItem>() {
            override fun areItemsTheSame(
                oldItem: StatisticsItem,
                newItem: StatisticsItem
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: StatisticsItem,
                newItem: StatisticsItem
            ): Boolean =
                oldItem == newItem
        }

        const val VIEW_TYPE_NETWORK = 0
        const val VIEW_TYPE_AD = 1
        const val VIEW_TYPE_VIDEO = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_AD
            itemCount -> VIEW_TYPE_NETWORK
            else -> VIEW_TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_AD -> {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ad, parent, false)
                AdHolder(mView)
            }
            else -> {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_general_video, parent, false)
                GeneralVideoViewHolder(mView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: StatisticsItem()
        when (holder) {
            is AdHolder -> {
                Glide.with(holder.adImg.context)
                    .load(item.adItem?.href)
                    .into(holder.adImg)

                holder.adImg.setOnClickListener {
                    GeneralUtils.openWebView(holder.adImg.context, item.adItem?.target ?: "")
                }
            }
            is GeneralVideoViewHolder -> {
                val options = RequestOptions()
                    .priority(Priority.NORMAL)
                    .placeholder(R.drawable.img_nopic_03)
                    .error(R.drawable.img_nopic_03)
                Glide.with(holder.videoImage.context).load(item.cover)
                    .apply(options)
                    .into(holder.videoImage)

                holder.videoTitleText.text = item.title
                holder.videoLayout.setOnClickListener { onItemClick(item) }
            }
        }
    }

    class GeneralVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoImage: ImageView = itemView.iv_video
        val videoTitleText: TextView = itemView.tv_title
        val videoLayout: ConstraintLayout = itemView.layout_video
    }
}