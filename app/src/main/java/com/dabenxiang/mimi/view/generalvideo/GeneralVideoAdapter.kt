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
import com.dabenxiang.mimi.model.api.vo.VideoByCategoryItem
import kotlinx.android.synthetic.main.item_general_video.view.*

class GeneralVideoAdapter(
    val onItemClick: (VideoByCategoryItem) -> Unit
) : PagingDataAdapter<VideoByCategoryItem, RecyclerView.ViewHolder>(COMPARATOR) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<VideoByCategoryItem>() {
            override fun areItemsTheSame(
                oldItem: VideoByCategoryItem,
                newItem: VideoByCategoryItem
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: VideoByCategoryItem,
                newItem: VideoByCategoryItem
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_general_video, parent, false)
        return GeneralVideoViewHolder(mView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as GeneralVideoViewHolder

        val item = getItem(position) ?: VideoByCategoryItem()

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

    class GeneralVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoImage: ImageView = itemView.iv_video
        val videoTitleText: TextView = itemView.tv_title
        val videoLayout: ConstraintLayout = itemView.layout_video
    }
}