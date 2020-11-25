package com.dabenxiang.mimi.view.mimi_home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.model.api.vo.ThirdMenuItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MiMiViewModel : BaseViewModel() {

    private val _menusItems = MutableLiveData<ApiResult<List<SecondMenuItem>>>()
    val menusItems: LiveData<ApiResult<List<SecondMenuItem>>> = _menusItems

    fun getMenu() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMenu()
                if (!result.isSuccessful) throw HttpException(result)

                val adItem = domainManager.getAdRepository()
                    .getAD(adWidth, adHeight).body()?.content ?: AdItem()

                val secondMenuItems = result.body()?.content?.get(0)?.menus
                val sortedSecondMenuItems = secondMenuItems?.sortedBy { item -> item.sorting }

                val thirdMenuItems: ArrayList<ThirdMenuItem> = arrayListOf()

                sortedSecondMenuItems?.forEach { item ->
                    item.menus.forEachIndexed { index, thirdMenuItem ->
                        if (index % 2 == 0 && index != 0) {
                            thirdMenuItems.add(ThirdMenuItem(adItem = adItem))
                        }
                        thirdMenuItems.add(thirdMenuItem)
                    }
                    item.menus = thirdMenuItems
                }

                emit(ApiResult.success(sortedSecondMenuItems))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _menusItems.value = it }
        }
    }
}