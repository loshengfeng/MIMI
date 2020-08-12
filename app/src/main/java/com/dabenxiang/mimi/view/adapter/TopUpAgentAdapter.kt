package com.dabenxiang.mimi.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.topup.TopUpProxyPayViewHolder

class TopUpAgentAdapter(
    private val listener: EventListener
) : PagedListAdapter<AgentItem, RecyclerView.ViewHolder>(diffCallback) {
    interface EventListener {
        fun onItemClick(view: View, item: AgentItem)
        fun onGetAvatarAttachment(id: String, position: Int)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<AgentItem>() {
            override fun areItemsTheSame(
                oldItem: AgentItem,
                newItem: AgentItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: AgentItem,
                newItem: AgentItem
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TopUpProxyPayViewHolder(
                layoutInflater.inflate(
                        R.layout.item_topup_proxy_pay,
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

    fun update(position: Int) {
        notifyItemChanged(position)
    }
}