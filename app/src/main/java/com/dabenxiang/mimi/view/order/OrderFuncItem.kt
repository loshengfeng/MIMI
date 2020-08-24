package com.dabenxiang.mimi.view.order

import androidx.paging.PagedList
import androidx.paging.PagingData
import com.dabenxiang.mimi.model.api.vo.OrderItem
import kotlinx.coroutines.CoroutineScope

class OrderFuncItem(
    val getOrder: (((PagingData<OrderItem>, CoroutineScope) -> Unit)) -> Unit = { _ -> },
    val getOrder2: (((PagedList<OrderItem>) -> Unit)) -> Unit = { _ -> }
)