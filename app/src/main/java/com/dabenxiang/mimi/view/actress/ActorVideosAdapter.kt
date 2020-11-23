package com.dabenxiang.mimi.view.actress

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ActorVideosItem
import com.dabenxiang.mimi.model.api.vo.OrderingPackageItem
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem

class ActorVideosAdapter(
    private val context: Context,
    private val listener: EventListener
) : RecyclerView.Adapter<ActorVideosViewHolder>() {

    private var selectItem: OrderingPackageItem? = null

    private var actorVideosItems: ArrayList<ActorVideosItem>? = null

    interface EventListener {
        fun onClickListener(item: ReferrerHistoryItem, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorVideosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topup_online_pay, parent, false)
        return ActorVideosViewHolder(view)
    }

    override fun getItemCount(): Int {
        return actorVideosItems?.size ?: 0
    }

    override fun onBindViewHolder(holder: ActorVideosViewHolder, position: Int) {
        val item = actorVideosItems?.get(position)
    }

    fun setupData(data: ArrayList<ActorVideosItem>) {
        actorVideosItems = data
    }

    fun clearSelectItem() {
        selectItem = null
    }

    fun getSelectItem(): OrderingPackageItem? {
        return selectItem
    }
}