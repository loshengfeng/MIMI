package com.dabenxiang.mimi.view.clubdetail

import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
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
    private val clNoData: ConstraintLayout = itemView.cl_no_data
    private var onParentFollowClick: ((MemberPostItem, List<MemberPostItem>, Boolean, ((Boolean) -> Unit)) -> Unit) =
        { _, _, _, _ -> }

    fun onBind(
        position: Int,
        clubDetailFuncItem: ClubDetailFuncItem,
        adultListener: AdultListener
    ): MemberPostPagedAdapter? {
        onParentFollowClick = clubDetailFuncItem.onFollowClick

        swipeRefreshLayout.setColorSchemeColors(swipeRefreshLayout.context.getColor(R.color.color_red_1))
        swipeRefreshLayout.setOnRefreshListener {
            clubDetailFuncItem.getMemberPost(getOrderType(position),
                { list -> updateList(list) },
                { count -> updateNoDataView(count) })
        }

        if (rvPost.adapter == null || rvPost.tag != position) {
            rvPost.tag = position
            val adapter = MemberPostPagedAdapter(
                rvPost.context,
                adultListener,
                "",
                MemberPostFuncItem(
                    onItemClick = {},
                    getBitmap = clubDetailFuncItem.getBitmap,
                    onFollowClick = { item, _, isFollow, _ -> onFollowClick(item, isFollow) },
                    onLikeClick = clubDetailFuncItem.onLikeClick
                )
            )
            rvPost.adapter = adapter

            clubDetailFuncItem.getMemberPost(getOrderType(position),
                { list -> updateList(list) },
                { count -> updateNoDataView(count) })
            swipeRefreshLayout.isRefreshing = true
            return adapter
        }
        return null
    }

    private fun onFollowClick(memberPostItem: MemberPostItem, isFollow: Boolean) {
        onParentFollowClick(
            memberPostItem,
            (rvPost.adapter as MemberPostPagedAdapter).currentList ?: arrayListOf(),
            isFollow
        ) { updateFollowStatus() }
    }

    private fun updateFollowStatus() {
        rvPost.adapter?.notifyDataSetChanged()
    }

    private fun updateList(list: PagedList<MemberPostItem>) {
        swipeRefreshLayout.isRefreshing = false
        (rvPost.adapter as MemberPostPagedAdapter).submitList(list)
    }

    private fun updateNoDataView(itemCount: Int) {
        clNoData.visibility = takeIf { itemCount > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
    }

    private fun getOrderType(position: Int): OrderBy {
        return when (position) {
            0 -> OrderBy.HOTTEST
            1 -> OrderBy.NEWEST
            else -> OrderBy.VIDEO
        }
    }
}