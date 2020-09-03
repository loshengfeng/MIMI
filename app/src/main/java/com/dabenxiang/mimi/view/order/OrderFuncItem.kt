package com.dabenxiang.mimi.view.order

import android.widget.ImageView
import androidx.paging.PagedList
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.OrderType
import kotlinx.coroutines.CoroutineScope

class OrderFuncItem(
    val getOrderByPaging3: (((PagingData<OrderItem>, CoroutineScope) -> Unit)) -> Unit = { _ -> },
    val getOrderByPaging2: (OrderType?, ((PagedList<OrderItem>) -> Unit)) -> Unit = { _, _ -> },
    val getChatList: (((PagedList<ChatListItem>) -> Unit)) -> Unit = { _ -> },
    val getChatAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onChatItemClick: ((ChatListItem) -> Unit) = { _ -> },
    val getOrderProxyAttachment: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onContactClick: ((ChatListItem, OrderItem) -> Unit) = { _, _ -> },
    val getProxyUnread: (((Int, Boolean) -> Unit) -> Unit) = { _ -> },
    val onTopUpClick: (() -> Unit) = {}
)