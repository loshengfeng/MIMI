package com.dabenxiang.mimi.view.order

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.OrderItem

class OrderListFactory constructor(
    private val orderListDataSource: OrderListDataSource
) : DataSource.Factory<Long, OrderItem>() {
    override fun create(): DataSource<Long, OrderItem> {
        return orderListDataSource
    }
}