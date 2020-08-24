package com.dabenxiang.mimi.view.order

import android.view.View
import androidx.paging.PagedList
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.adapter.FavoriteTabAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import com.dabenxiang.mimi.view.favroite.FavoriteFragment
import kotlinx.android.synthetic.main.item_order_pager.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class OrderPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val rvTab: RecyclerView = itemView.rv_tab
    private val swipeRefreshLayout: SwipeRefreshLayout = itemView.swipeRefreshLayout
    private val rvOrder: RecyclerView = itemView.rv_order
    private val rvChat: RecyclerView = itemView.rv_chat
    private val clNoData: View = itemView.item_no_data

    private var orderFuncItem: OrderFuncItem? = null

    private val tabAdapter by lazy {
        FavoriteTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(index)
            }
        }, false)
    }
    private val orderAdapter by lazy { OrderAdapter() }
    private val chatAdapter by lazy { ChatHistoryAdapter(listener) }
    private val listener = object : ChatHistoryAdapter.EventListener {
        override fun onClickListener(item: ChatListItem) {
            Timber.d("@@onClickListener $item")
            orderFuncItem?.onChatItemClick?.invoke(item)
        }

        override fun onGetAttachment(id: String, position: Int) {
            orderFuncItem?.getChatAttachment?.invoke(id, position) { pos -> updateChatAvatar(pos) }
        }
    }

    private fun setTabPosition(index: Int) {
        tabAdapter.setLastSelectedIndex(index)
        when(index) {
            0 -> {
                rvOrder.visibility = View.VISIBLE
                rvChat.visibility = View.GONE
            }
            else -> {
                rvOrder.visibility = View.GONE
                rvChat.visibility = View.VISIBLE
            }
        }
    }

    fun onBind(position: Int, orderFuncItem: OrderFuncItem) {
        when(position) {
            2 -> {
                if (rvChat.adapter == null) {
                    rvChat.adapter = chatAdapter
                    orderFuncItem.getChatList { list -> updateChatList(list) }
                }
                if (rvTab.adapter == null) {
                    val secondaryList = listOf(
                        App.self.getString(R.string.topup_proxy_order),
                        App.self.getString(R.string.topup_proxy_chat)
                    )
                    tabAdapter.submitList(secondaryList, FavoriteFragment.lastSecondaryIndex)
                    rvTab.adapter = tabAdapter
                }
                rvTab.visibility = View.VISIBLE

            }
            else -> rvTab.visibility = View.GONE
        }

        if (rvOrder.adapter == null || rvOrder.tag != position) {
            rvOrder.tag = position
            rvOrder.adapter = orderAdapter
//            orderFuncItem.getOrderByPaging3 { data, scope -> updateOrderList3(data, scope) }
            orderFuncItem.getOrderByPaging2(getOnlineStatus(position)) { list -> updateOrderList2(list) }
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun updateOrderList2(list: PagedList<OrderItem>) {
        swipeRefreshLayout.isRefreshing = false
        orderAdapter.submitList(list)
        clNoData.visibility = takeIf { list.size > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
    }

    private fun updateOrderList3(data: PagingData<OrderItem>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
//            (rvOrder.adapter as OrderAdapter).submitData(data)
        }
        swipeRefreshLayout.isRefreshing = false
//        clNoData.visibility = takeIf { list. }?.let { View.GONE } ?: let { View.VISIBLE }
    }

    private fun updateChatList(list: PagedList<ChatListItem>) {
        swipeRefreshLayout.isRefreshing = false
        chatAdapter.submitList(list)
        clNoData.visibility = takeIf { list.size > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
    }

    private fun updateChatAvatar(position: Int) {
        Timber.d("@@updateChatAvatar: $position")
        chatAdapter.update(position)
    }

    private fun getOnlineStatus(position: Int): Boolean? {
        return when(position) {
            0 -> null
            1 -> true
            else -> false
        }
    }
}