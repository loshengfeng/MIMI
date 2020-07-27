package com.dabenxiang.mimi.view.mypost

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class MyPostFactory constructor(
    private val myPostDataSource: MyPostDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return myPostDataSource
    }
}
