package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.topup.TopUpProxyPayViewHolder

class TopUpAgentAdapter(
    private val listener: AdapterEventListener<AgentItem>
) : PagedListAdapter<Any, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Any,
                newItem: Any
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TopUpProxyPayViewHolder(
                layoutInflater.inflate(
                        R.layout.item_favorite_normal,
                        parent,
                        false
                ), listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TopUpProxyPayViewHolder -> holder.bind(item as AgentItem)
        }
    }
}