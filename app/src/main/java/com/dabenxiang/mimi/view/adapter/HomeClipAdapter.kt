package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.ClipViewHolder

class HomeClipAdapter(
    nestedListener: HomeAdapter.EventListener,
    private val memberPostFuncItem: MemberPostFuncItem
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var memberPostItems: List<MemberPostItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_clip, parent, false)
        return ClipViewHolder(view, clipClickListener, memberPostFuncItem)
    }

    override fun getItemCount(): Int {
        return if(memberPostItems.size > 10) 10 else memberPostItems.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as ClipViewHolder
        val memberPostItem = memberPostItems[position]
        holder.bind(memberPostItem, position)
    }

    fun submitList(list: List<MemberPostItem>) {
        memberPostItems = list
        notifyDataSetChanged()
    }

    private val clipClickListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                nestedListener.onClipClick(view, memberPostItems, index)
            }
        }
    }
}