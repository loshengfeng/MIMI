package com.dabenxiang.mimi.view.clubdetail

import android.view.View
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.AdultListener
import com.dabenxiang.mimi.callback.MemberPostFuncItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_club_pager.view.*

class ClubPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val swipeRefreshLayout: SwipeRefreshLayout = itemView.swipeRefreshLayout
    private val rvPost: RecyclerView = itemView.rv_post

    fun onBind(
        position: Int,
        clubDetailFuncItem: ClubDetailFuncItem,
        adultListener: AdultListener
    ) {
        swipeRefreshLayout.setColorSchemeColors(swipeRefreshLayout.context.getColor(R.color.color_red_1))
        swipeRefreshLayout.setOnRefreshListener {
            clubDetailFuncItem.getMemberPost(getOrderType(position)) { list -> updateList(list) }
        }

        if (rvPost.adapter == null || rvPost.tag != position) {
            rvPost.tag = position
            rvPost.adapter =
                MemberPostPagedAdapter(
                    rvPost.context,
                    adultListener,
                    "",
                    MemberPostFuncItem(onItemClick = {},
                        getBitmap = clubDetailFuncItem.getBitmap,
                        onFollowClick = clubDetailFuncItem.onFollowClick,
                        onLikeClick = clubDetailFuncItem.onLikeClick)
                )
            clubDetailFuncItem.getMemberPost(getOrderType(position)) { list -> updateList(list) }
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun updateList(list: PagedList<MemberPostItem>) {
        swipeRefreshLayout.isRefreshing = false
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