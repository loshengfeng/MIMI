package com.dabenxiang.mimi.view.club.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
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

    inner class TopicViewHolder(itemview: View): RecyclerView.ViewHolder(itemview) {
        val topic_title = itemview.topic_title
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_tab, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        getItem(position)?.let {item->
            holder.topic_title.text = item.title
            holder.itemView.setOnClickListener {
                listener.itemClicked(item, position)
            }
        }
    }

//    fun setTestData(){
//        val datas =  listOf<MemberClubItem>(
//            MemberClubItem(title= "Topic 1"),
//            MemberClubItem(title= "Topic 2"),
//            MemberClubItem(title= "Topic 3"),
//            MemberClubItem(title= "Topic 4"),
//            MemberClubItem(title= "Topic 5"),
//            MemberClubItem(title= "Topic 6")
//        )
//        submitData(datas)
//    }


}

interface TopicItemListener {
    fun itemClicked(item: MemberClubItem, position: Int)
}
