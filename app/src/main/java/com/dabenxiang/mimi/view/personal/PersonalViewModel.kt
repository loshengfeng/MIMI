package com.dabenxiang.mimi.view.personal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.dabenxiang.mimi.APK_NAME
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import retrofit2.HttpException
import timber.log.Timber
import tw.gov.president.manager.submanager.update.VersionManager
import tw.gov.president.manager.submanager.update.callback.DownloadProgressCallback
import tw.gov.president.manager.submanager.update.data.VersionStatus

class PersonalViewModel : BaseViewModel() {

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem: LiveData<ApiResult<MeItem>> = _meItem

    private val _apiSignOut = MutableLiveData<ApiResult<Nothing>>()
    val apiSignOut: LiveData<ApiResult<Nothing>> = _apiSignOut

    private val _unreadResult = MutableLiveData<ApiResult<Int>>()
    val unreadResult: LiveData<ApiResult<Int>> = _unreadResult

    private val _versionStatus = MutableLiveData<VersionStatus>()
    val versionStatus: LiveData<VersionStatus> = _versionStatus

    private val versionManager: VersionManager by inject()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e("$throwable")
    }

    fun getMemberInfo() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                val meItem = result.body()?.content
                meItem?.let {
                    accountManager.setupProfile(it)
                }
                emit(ApiResult.success(meItem))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _meItem.value = it }
        }
    }

    fun getProfile(): ProfileItem {
        return pref.profileItem
    }

    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut().collect {
                _apiSignOut.value = it
            }
        }
    }

    fun getUnread() {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getUnread()
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content as Int))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _unreadResult.value = it }
        }
    }

    private val _totalUnreadResult = MutableLiveData<ApiResult<Int>>()
    val totalUnreadResult: LiveData<ApiResult<Int>> = _totalUnreadResult

    fun getTotalUnread() {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val chatUnreadResult = apiRepository.getUnread()
                val chatUnread =
                    if (!chatUnreadResult.isSuccessful) 0 else chatUnreadResult.body()?.content ?: 0
                val orderUnreadResult = apiRepository.getUnReadOrderCount()
                val orderUnread =
                    if (!orderUnreadResult.isSuccessful) 0 else orderUnreadResult.body()?.content
                        ?: 0
                emit(ApiResult.success(chatUnread + orderUnread))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _totalUnreadResult.value = it }
        }
    }

    fun getOldDriverUrl(): String {
        return domainManager.getOldDriverUrl()
    }

    fun checkVersion() {
        viewModelScope.launch(handler) {
            Timber.i("checkVersion")
            flow {
                val versionStatus = versionManager.checkVersion()
                delay(100)
                emit(versionStatus)
            }.flowOn(Dispatchers.IO).collect {
                Timber.i("checkVersion = $it")
                _versionStatus.value = it
            }
        }
    }

    fun updateApp(progressCallback: DownloadProgressCallback) {
        viewModelScope.launch {
            flow {
                versionManager.updateApp(APK_NAME, progressCallback)
                emit(null)
            }.flowOn(Dispatchers.IO).collect { Timber.d("Update!") }
        }
    }

}