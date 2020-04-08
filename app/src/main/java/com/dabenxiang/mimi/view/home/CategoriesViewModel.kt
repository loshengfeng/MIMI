package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.view.base.BaseViewModel2
import org.koin.core.inject

class CategoriesViewModel : BaseViewModel2() {

    private val apiRepository: ApiRepository by inject()
}