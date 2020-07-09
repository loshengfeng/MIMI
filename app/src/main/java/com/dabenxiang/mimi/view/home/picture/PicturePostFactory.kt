package com.dabenxiang.mimi.view.home.picture

import androidx.paging.DataSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class PicturePostFactory constructor(
    private val picturePostDataSource: PicturePostDataSource
) : DataSource.Factory<Int, MemberPostItem>() {
    override fun create(): DataSource<Int, MemberPostItem> {
        return picturePostDataSource
    }
}
