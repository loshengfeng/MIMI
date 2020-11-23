package com.dabenxiang.mimi.view.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ThirdMenuItem
import kotlinx.android.synthetic.main.item_recommend.view.*
import timber.log.Timber

class RecommendContentAdapter(
    private val thirdMenuItems: List<ThirdMenuItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend, parent, false)
        return RecommendViewHolder(mView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as RecommendViewHolder

        val item = thirdMenuItems[position]

        holder.titleText.text = item.name
        holder.moreText.setOnClickListener { Timber.d("@@More......") }
        holder.recommendContentRecycler.adapter = RecommendVideoAdapter(item.videos)

        if (item.videos.isEmpty()) {
            holder.titleText.visibility = View.GONE
            holder.moreText.visibility = View.GONE
        } else {
            holder.titleText.visibility = View.VISIBLE
            holder.moreText.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return thirdMenuItems.size
    }

    class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView = itemView.tv_title
        var moreText: TextView = itemView.tv_more
        val recommendContentRecycler: RecyclerView = itemView.rv_recommend_content
    }
}