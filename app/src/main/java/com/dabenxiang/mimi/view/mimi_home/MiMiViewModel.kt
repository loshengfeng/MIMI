package com.dabenxiang.mimi.view.mimi_home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.SubMenuItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MiMiViewModel : BaseViewModel() {

    private val _menusItems = MutableLiveData<ApiResult<List<SubMenuItem>>>()
    val menusItems: LiveData<ApiResult<List<SubMenuItem>>> = _menusItems

    fun getMenu() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMenu()
                if (!result.isSuccessful) throw HttpException(result)
                val subMenuItems = result.body()?.content?.get(0)?.menus
                val sortedSubMenuItems = subMenuItems?.sortedBy { item -> item.sorting }
                emit(ApiResult.success(sortedSubMenuItems))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _menusItems.value = it }
        }
    }
}