package com.dabenxiang.mimi.view.recommend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.CategoryBanner
import com.dabenxiang.mimi.model.enums.AdultType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RecommendViewModel : BaseViewModel() {

    private val _bannerItems = MutableLiveData<ApiResult<List<CategoryBanner>>>()
    val bannerItems: LiveData<ApiResult<List<CategoryBanner>>> = _bannerItems

    fun getBanners() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .fetchHomeBanner(AdultType.Adult.value)
                if (!result.isSuccessful) throw HttpException(result)
                val categoryBanners = result.body()?.content
                emit(ApiResult.success(categoryBanners))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _bannerItems.value = it }
        }
    }


}