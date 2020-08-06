package com.dabenxiang.mimi.view.dialog.chooseclub

import androidx.paging.DataSource

class ChooseClubFactory constructor(
    private val chooseClubFactory: ChooseClubDataSource
) : DataSource.Factory<Long, Any>() {
    override fun create(): DataSource<Long, Any> {
        return chooseClubFactory
    }
}