package com.dabenxiang.mimi.view.mimi_home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.SecondMenusItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MiMiViewModel : BaseViewModel() {

    private val _menusItems = MutableLiveData<ApiResult<List<SecondMenusItem>>>()
    val menusItems: LiveData<ApiResult<List<SecondMenusItem>>> = _menusItems

    fun getMenu() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMenu()
                if (!result.isSuccessful) throw HttpException(result)
                val secondMenuItems = result.body()?.content?.get(0)?.menus
                val sortedMenuItems = secondMenuItems?.sortedBy { item -> item.sorting }
                emit(ApiResult.success(sortedMenuItems))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _menusItems.value = it }
        }
    }
}