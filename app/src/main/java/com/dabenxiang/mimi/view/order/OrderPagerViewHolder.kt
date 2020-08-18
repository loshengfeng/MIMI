package com.dabenxiang.mimi.view.order

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.item_order_pager.view.*

class OrderPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val swipeRefreshLayout: SwipeRefreshLayout = itemView.swipeRefreshLayout
    private val rvPost: RecyclerView = itemView.rv_post
    private val clNoData: View = itemView.item_no_data

    fun onBind( position: Int) {
        GeneralUtils.showToast(App.self, "$position")
    }
}