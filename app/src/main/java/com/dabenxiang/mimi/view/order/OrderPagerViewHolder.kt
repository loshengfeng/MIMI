package com.dabenxiang.mimi.view.order

import android.view.View
import androidx.paging.PagedList
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_order_pager.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class OrderPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val swipeRefreshLayout: SwipeRefreshLayout = itemView.swipeRefreshLayout
    private val rvOrder: RecyclerView = itemView.rv_order
    private val clNoData: View = itemView.item_no_data

    fun onBind(position: Int, orderFuncItem: OrderFuncItem) {
        if (rvOrder.adapter == null || rvOrder.tag != position) {
            rvOrder.tag = position
            rvOrder.adapter = OrderAdapter()
//            orderFuncItem.getOrder { data, scope -> updateList(data, scope) }
            orderFuncItem.getOrder2 { list -> updateList2(list) }
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun updateList(data: PagingData<OrderItem>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            Timber.d("@@updateList: $data")
//            (rvOrder.adapter as OrderAdapter).submitData(data)
        }
        clNoData.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = false
//        clNoData.visibility = takeIf { list. }?.let { View.GONE } ?: let { View.VISIBLE }
    }

    private fun updateList2(list: PagedList<OrderItem>) {
        swipeRefreshLayout.isRefreshing = false
        (rvOrder.adapter as OrderAdapter).submitList(list)
        clNoData.visibility = takeIf { list.size > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
    }
}