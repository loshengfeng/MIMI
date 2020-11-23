package com.dabenxiang.mimi.view.club.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.PostItem
import kotlinx.android.synthetic.main.item_topic_tab.view.*

class TopicTabAdapter : RecyclerView.Adapter<TopicTabAdapter.TopicViewHolder>() {

    private var viewData =  listOf<PostItem>()

    fun submitData(list: List<PostItem>) {
        viewData = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_tab, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        viewData[position]?.let {
            holder.tabTitle.text = it.title
        }

    }

    override fun getItemCount(): Int = viewData.size

    inner class TopicViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val tabBg = itemView.iv_topic
            val tabTitle = itemView.topic_title

    }

    fun setTestData(){
        val datas =  listOf<PostItem>(
                PostItem(title= "Topic 1"),
                PostItem(title= "Topic 2"),
                PostItem(title= "Topic 3"),
                PostItem(title= "Topic 4"),
                PostItem(title= "Topic 5"),
                PostItem(title= "Topic 6")
        )
        submitData(datas)
    }

}