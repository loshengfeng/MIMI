package com.dabenxiang.mimi.view.dialog.chooseclub

import androidx.paging.DataSource

class ChooseClubFactory constructor(
    private val chooseClubFactory: ChooseClubDataSource
) : DataSource.Factory<Int, Any>() {
    override fun create(): DataSource<Int, Any> {
        return chooseClubFactory
    }
}