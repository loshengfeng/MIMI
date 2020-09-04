package com.dabenxiang.mimi.view.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.model.api.vo.MemberFollowItem
import com.dabenxiang.mimi.view.adapter.MemberFollowAdapter
import com.dabenxiang.mimi.view.base.BaseAnyViewHolder
import kotlinx.android.synthetic.main.item_follow_member.view.*

class MemberFollowViewHolder(
    itemView: View,
    val listener: MemberFollowAdapter.EventListener
) : BaseAnyViewHolder<MemberFollowItem>(itemView) {
    private val ivPhoto: ImageView = itemView.iv_photo
    private val tvName: TextView = itemView.tv_name
    private val tvSubTitle: TextView = itemView.tv_sub_title
    private val clFollow: ConstraintLayout = itemView.cl_follow

    init {
        itemView.setOnClickListener { data?.let { data -> listener.onDetail(data) } }
    }

    override fun updated(position: Int) {
        data?.avatarAttachmentId?.let { id -> listener.onGetAttachment(id, ivPhoto) }
        tvName.text = data?.friendlyName
        tvSubTitle.text = data?.friendlyName
        clFollow.setOnClickListener {
            data?.userId?.let { userId ->
                listener.onCancelFollow(
                    userId,
                    position
                )
            }
        }
    }

    override fun updated() {
    }
}