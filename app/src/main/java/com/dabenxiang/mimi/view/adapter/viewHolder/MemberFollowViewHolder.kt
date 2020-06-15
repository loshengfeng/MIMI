package com.dabenxiang.mimi.view.adapter.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem

class MemberFollowViewHolder(parent :ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_follow, parent, false)) {

    private val ivPhoto = itemView.findViewById(R.id.iv_photo) as ImageView
    private val tvName = itemView.findViewById(R.id.tv_name) as TextView
//    private val tvSubTitle = itemView.findViewById(R.id.tv_sub_title) as TextView
//    private val tvFollow = itemView.findViewById(R.id.tv_follow) as TextView

    var memberFollowItem : MemberFollowItem? = null

    fun bindTo(memberFollowItem: MemberFollowItem) {
        this.memberFollowItem = memberFollowItem
        tvName.text = memberFollowItem?.friendlyName
//        tvSubTitle.text = clubFollowItem?.description
//        tvFollow.text = clubFollowItem?.tag
    }

    fun bind(memberFollowItem: MemberFollowItem?) {
        this.memberFollowItem = memberFollowItem
        tvName.text = memberFollowItem?.friendlyName ?: ""
//        tvSubTitle.text = clubFollowItem?.description
//        tvFollow.text = clubFollowItem?.tag
    }
}