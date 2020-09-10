package com.dabenxiang.mimi.view.order

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedList
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.OrderType
import com.dabenxiang.mimi.view.adapter.ChatHistoryAdapter
import com.dabenxiang.mimi.view.base.BaseIndexViewHolder
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_order_no_data.view.*
import kotlinx.android.synthetic.main.item_order_pager.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class OrderPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val rvTab: RecyclerView = itemView.rv_tab
    private val swipeRefreshLayout: SwipeRefreshLayout = itemView.swipeRefreshLayout
    private val rvOrder: RecyclerView = itemView.rv_order
    private val rvChat: RecyclerView = itemView.rv_chat
    private val itemOrderNoData: View = itemView.item_order_no_data
    private val itemChatNoData: View = itemView.item_chat_no_data
    private val tvTopUp: TextView = itemView.tv_topup

    private var orderFuncItem: OrderFuncItem? = null

    private val tabAdapter by lazy {
        ProxyTabAdapter(object : BaseIndexViewHolder.IndexViewHolderListener {
            override fun onClickItemIndex(view: View, index: Int) {
                setTabPosition(index)
            }
        })
    }

    private val tabList by lazy {
        mutableListOf(
            Pair(App.self.getString(R.string.topup_proxy_order), false),
            Pair(App.self.getString(R.string.topup_proxy_chat), false)
        )
    }

    private val orderAdapter by lazy { OrderAdapter(orderFuncItem) }
    private val chatAdapter by lazy { ChatHistoryAdapter(listener) }
    private val listener = object : ChatHistoryAdapter.EventListener {
        override fun onClickListener(item: ChatListItem, position: Int) {
            updateChatItem(item, position)
            orderFuncItem?.onChatItemClick?.invoke(item)
        }

        override fun onGetAttachment(id: Long?, view: ImageView) {
            orderFuncItem?.getChatAttachment?.invoke(id, view)
        }
    }

    private fun updateChatItem(item: ChatListItem, position: Int) {
        item.lastReadTime = Date(System.currentTimeMillis())
        chatAdapter.update(position)
    }

    private fun setTabPosition(index: Int) {
        tabAdapter.setLastSelectedIndex(index)
        when(index) {
            0 -> {
                rvOrder.visibility = View.VISIBLE
                rvChat.visibility = View.GONE
                itemChatNoData.visibility = View.GONE
                itemOrderNoData.visibility =
                    takeIf { orderAdapter.currentList?.size ?: 0 > 0 }?.let { View.GONE }
                        ?: let { View.VISIBLE }
            }
            else -> {
                rvOrder.visibility = View.GONE
                rvChat.visibility = View.VISIBLE
                itemOrderNoData.visibility = View.GONE
                itemChatNoData.visibility =
                    takeIf { chatAdapter.currentList?.size ?: 0 > 0 }?.let { View.GONE }
                        ?: let { View.VISIBLE }
            }
        }
    }

    fun onBind(position: Int, orderFuncItem: OrderFuncItem) {
        this.orderFuncItem = orderFuncItem
        when(position) {
            2 -> {
                if (rvChat.adapter == null) {
                    rvChat.adapter = chatAdapter
                    orderFuncItem.getChatList { list -> updateChatList(list) }
                }
                if (rvTab.adapter == null) {
                    tabAdapter.submitList(tabList, 0)
                    rvTab.adapter = tabAdapter
                }
                rvTab.visibility = View.VISIBLE
                orderFuncItem.getProxyUnread { pos, isNew -> updateUnread(pos, isNew) }
            }
            else -> rvTab.visibility = View.GONE
        }

        if (rvOrder.adapter == null || rvOrder.tag != position) {
            rvOrder.tag = position
            rvOrder.adapter = orderAdapter
//            orderFuncItem.getOrderByPaging3 { data, scope -> updateOrderList3(data, scope) }
            orderFuncItem.getOrderByPaging2(getOrderType(position)) { list -> updateOrderList2(list) }
            swipeRefreshLayout.isRefreshing = true
        }

        swipeRefreshLayout.setOnRefreshListener {
            itemOrderNoData.visibility = View.GONE
            itemChatNoData.visibility = View.GONE
            when(position) {
                2 -> {
                    when(tabAdapter.getSelectedPosition()) {
                        1 -> {
                            orderFuncItem.getChatList { list -> updateChatList(list) }
                        }
                        else -> {
                            orderFuncItem.getOrderByPaging2(getOrderType(position)) { list ->
                                updateOrderList2(list)
                            }
                        }
                    }
                }
                else ->
                    orderFuncItem.getOrderByPaging2(getOrderType(position)) { list ->
                        updateOrderList2(list)
                    }
            }
        }

        tvTopUp.setOnClickListener {
            orderFuncItem.onTopUpClick.invoke()
        }
    }

    private fun updateOrderList2(list: PagedList<OrderItem>) {
        swipeRefreshLayout.isRefreshing = false
        orderAdapter.submitList(list)
        itemOrderNoData.visibility = takeIf { list.size > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
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
        itemChatNoData.visibility = takeIf { list.size > 0 }?.let { View.GONE } ?: let { View.VISIBLE }
    }

    private fun updateChatAvatar(position: Int) {
        chatAdapter.update(position)
    }

    private fun getOrderType(position: Int): OrderType? {
        return when(position) {
            0 -> null
            1 -> OrderType.USER2ONLINE
            else -> OrderType.MERCHANT2USER
        }
    }

    private fun updateUnread(position: Int, isNew: Boolean) {
        val pair = tabList[position]
        tabList[position] = Pair(pair.first, isNew)
        tabAdapter.notifyItemChanged(position)
    }
}