package com.dabenxiang.mimi.view.home

import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.view.base.BaseViewModel
import org.koin.core.inject

class CategoriesViewModel : BaseViewModel() {

    private val apiRepository: ApiRepository by inject()
}