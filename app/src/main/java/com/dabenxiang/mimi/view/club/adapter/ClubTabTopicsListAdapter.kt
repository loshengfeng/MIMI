package com.dabenxiang.mimi.view.club.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import kotlinx.android.synthetic.main.item_topic_tab.view.*

class TopicListAdapter(private val listener: TopicItemListener)
    : PagingDataAdapter<MemberClubItem, TopicListAdapter.TopicViewHolder>(diffCallback) {

      companion object{
          private val diffCallback = object : DiffUtil.ItemCallback<MemberClubItem>() {
              override fun areItemsTheSame(
                  oldItem: MemberClubItem,
                  newItem: MemberClubItem
              ): Boolean = oldItem.id == newItem.id

              override fun areContentsTheSame(
                  oldItem: MemberClubItem,
                  newItem: MemberClubItem
              ): Boolean = oldItem == newItem
          }
      }

    inner class TopicViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val topicTitle: TextView = itemView.topic_title
        val topicBg: ImageView = itemView.topic_bg
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_tab, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        getItem(position)?.let {item->
            holder.topicTitle.text = item.title
            holder.itemView.setOnClickListener {
                listener.itemClicked(item, position)
            }

            listener.getAttachment(item.avatarAttachmentId, holder.topicBg, LoadImageType.CLUB_TOPIC)
        }
    }
}

interface TopicItemListener {
    fun itemClicked(item: MemberClubItem, position: Int)
    fun getAttachment(id: Long?, view: ImageView, type: LoadImageType)
}
