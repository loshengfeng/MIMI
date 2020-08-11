package com.dabenxiang.mimi.view.topup

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.AgentItem

class TopUpProxyPayListFactory constructor(
    private val topUpProxyPayListDataSource: TopUpProxyPayListDataSource
) : DataSource.Factory<Long, AgentItem>() {
    override fun create(): DataSource<Long, AgentItem> {
        return topUpProxyPayListDataSource
    }
}