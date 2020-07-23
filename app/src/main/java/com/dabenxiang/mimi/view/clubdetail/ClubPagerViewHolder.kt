package com.dabenxiang.mimi.view.clubdetail

import android.view.View
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.AttachmentListener
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_club_pager.view.*

class ClubPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    val rvPost = itemView.rv_post

    fun onBind(
        position: Int,
        clubDetailFuncItem: ClubDetailFuncItem,
        attachmentListener: AttachmentListener,
        adultListener: AdultListener
    ) {
        if (rvPost.adapter == null || rvPost.tag != position) {
            rvPost.tag = position
            rvPost.adapter =
                MemberPostPagedAdapter(rvPost.context, adultListener, attachmentListener)
            clubDetailFuncItem.getMemberPost(getOrderType(position)) { list -> updateList(list) }
        }
    }

    private fun updateList(list: PagedList<MemberPostItem>) {
        (rvPost.adapter as MemberPostPagedAdapter).submitList(list)
    }

    private fun getOrderType(position: Int): OrderBy {
        return when (position) {
            0 -> OrderBy.HOTTEST
            1 -> OrderBy.NEWEST
            else -> OrderBy.VIDEO
        }
    }
}