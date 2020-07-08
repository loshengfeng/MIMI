package com.dabenxiang.mimi.view.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.home.viewholder.ClubViewHolder

class HomeClubAdapter(
    nestedListener: HomeAdapter.EventListener,
    private val attachmentListener: HomeAdapter.AttachmentListener,
    private val attachmentMap: HashMap<Long, Bitmap>
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var memberClubItems: List<MemberClubItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nested_item_home_club, parent, false)
        return ClubViewHolder(
            view,
            clubClickListener,
            parent.context,
            attachmentListener,
            attachmentMap
        )
    }

    override fun getItemCount(): Int {
        return memberClubItems.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder as ClubViewHolder
        val memberClubItem = memberClubItems[position]
        holder.bind(memberClubItem, position)
    }

    fun submitList(list: List<MemberClubItem>) {
        memberClubItems = list
        notifyDataSetChanged()
    }

    private val clubClickListener by lazy {
        object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                nestedListener.onClubClick(view, memberClubItems[index])
            }
        }
    }
}