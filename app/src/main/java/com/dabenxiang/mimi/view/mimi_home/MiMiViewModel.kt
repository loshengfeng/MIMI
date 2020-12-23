package com.dabenxiang.mimi.view.mimi_home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AnnounceConfigItem
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MiMiViewModel : BaseViewModel() {

    private val _menusItems = MutableLiveData<ApiResult<List<SecondMenuItem>>>()
    val menusItems: LiveData<ApiResult<List<SecondMenuItem>>> = _menusItems

    private val _inviteVipShake = MutableLiveData<Boolean>()
    val inviteVipShake: LiveData<Boolean> = _inviteVipShake

    private val _announceConfig = MutableLiveData<AnnounceConfigItem>()
    val announceConfig: LiveData<AnnounceConfigItem> = _announceConfig

    fun getMenu() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMenu()
                if (!result.isSuccessful) throw HttpException(result)

                val secondMenuItems = result.body()?.content?.get(0)?.menus
                val sortedSecondMenuItems = secondMenuItems?.sortedBy { item -> item.sorting }

                emit(ApiResult.success(sortedSecondMenuItems))
            }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _menusItems.value = it }
        }
    }

    fun startAnim(interval: Long) {
        viewModelScope.launch {
            _inviteVipShake.postValue(true)
            delay(interval)
            _inviteVipShake.postValue(false)
        }
    }

    fun getAnnounceConfig() {
        viewModelScope.launch {
            val result = domainManager.getApiRepository().getAnnounceConfigs()
            if (!result.isSuccessful) Timber.e(HttpException(result))
            result.body()?.content?.first()?.let { _announceConfig.postValue(it) }
        }
    }
}