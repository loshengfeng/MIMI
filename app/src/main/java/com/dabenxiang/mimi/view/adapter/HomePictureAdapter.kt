package com.dabenxiang.mimi.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.PictureViewHolder
import timber.log.Timber

class HomePictureAdapter(
    nestedListener: HomeAdapter.EventListener,
    private val memberPostFuncItem: MemberPostFuncItem
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var memberPostItems: List<MemberPostItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_picture, parent, false)
        return PictureViewHolder(view, pictureClickListener, memberPostFuncItem)
    }

    override fun getItemCount(): Int {
        return memberPostItems.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as PictureViewHolder
        val memberPostItem = memberPostItems[position]
        holder.bind(memberPostItem, position)
    }

    fun submitList(list: List<MemberPostItem>) {
        memberPostItems = list
        notifyDataSetChanged()
    }

    private val pictureClickListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                nestedListener.onPictureClick(view, memberPostItems[index])
            }
        }
    }

}