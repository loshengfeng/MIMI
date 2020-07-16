package com.dabenxiang.mimi.view.topup

import androidx.paging.DataSource

class TopUpProxyPayListFactory constructor(
    private val topUpProxyPayListDataSource: TopUpProxyPayListDataSource
) : DataSource.Factory<Long, Any>() {
    override fun create(): DataSource<Long, Any> {
        return topUpProxyPayListDataSource
    }
}