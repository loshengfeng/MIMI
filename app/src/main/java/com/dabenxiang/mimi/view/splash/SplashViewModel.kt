package com.dabenxiang.mimi.view.splash

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.APK_NAME
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.StatisticsRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.FileUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber
import tw.gov.president.manager.submanager.update.VersionManager
import tw.gov.president.manager.submanager.update.callback.DownloadProgressCallback
import tw.gov.president.manager.submanager.update.data.VersionStatus

class SplashViewModel : BaseViewModel() {

    private val _autoLoginResult = MutableLiveData<ApiResult<Nothing>>()
    val autoLoginResult: LiveData<ApiResult<Nothing>> = _autoLoginResult

    private val _versionStatus = MutableLiveData<VersionStatus>()
    val versionStatus: LiveData<VersionStatus> = _versionStatus

    private val _apiError: MutableLiveData<Boolean> = MutableLiveData()
    val apiError: LiveData<Boolean> = _apiError

    private val versionManager: VersionManager by inject()

    var isChecked =false

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e("$throwable")
        _apiError.postValue(true)
    }


    fun autoLogin() {
        if (accountManager.hasMemberToken()) {
            viewModelScope.launch {
                val profile = accountManager.getProfile()
                if (TextUtils.isEmpty(profile.account) || TextUtils.isEmpty(profile.password)) {
                    accountManager.logoutLocal()
                    _autoLoginResult.value = ApiResult.success(null)
                } else {
                    signIn(profile.account, profile.password)
                }
            }
        } else {
            _autoLoginResult.value = ApiResult.success(null)
        }
    }

    private suspend fun signIn(account: String, password: String) {
        accountManager.signIn(account, password)
            .collect { _autoLoginResult.value = it }
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

    fun isUpgradeApp(): Boolean {
        val recordTimestamp = versionManager.getRecordTimestamp()
        val hour = ((System.currentTimeMillis() - recordTimestamp) / 1000) / 60 / 60
        val result = hour > 24
        return result
    }

    fun updateApp(progressCallback: DownloadProgressCallback) {
        viewModelScope.launch {
            flow {
                versionManager.updateApp(APK_NAME, progressCallback)
                emit(null)
            }.flowOn(Dispatchers.IO).collect { Timber.d("Update!") }
        }
    }

    fun firstTimeStatistics(context: Context, promoteCode: String) {
        viewModelScope.launch {
            flow {
                val request = StatisticsRequest(code = promoteCode)
                val resp = domainManager.getAdRepository().statistics(request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> Timber.e("firstTimeStatistics error: $e") }
                .collect { FileUtil.createSecreteFile(context) }
        }
    }

    fun setupRecordTimestamp() {
        versionManager.setupRecordTimestamp()
    }

}