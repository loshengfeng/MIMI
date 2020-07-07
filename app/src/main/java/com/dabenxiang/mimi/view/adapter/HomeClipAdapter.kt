package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.ClipViewHolder

class HomeClipAdapter(
    nestedListener: HomeAdapter.EventListener
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var memberPostItems: List<MemberPostItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_clip, parent, false)
        return ClipViewHolder(view, clipClickListener)
    }

    override fun getItemCount(): Int {
        return memberPostItems.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as ClipViewHolder
        val realPosition = getRealPosition(position)
        val memberPostItem = memberPostItems[position]
        holder.bind(memberPostItem, realPosition)
    }

    fun submitList(list: List<MemberPostItem>) {
        memberPostItems = list
        notifyDataSetChanged()
    }

    private fun getRealPosition(position: Int): Int {
        val count = memberPostItems.count()
        return when {
            count == 0 -> 0
            position > count - 1 -> position % count
            else -> position
        }
    }

    private val clipClickListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                nestedListener.onClipClick(view, memberPostItems[index])
            }
        }
    }

}