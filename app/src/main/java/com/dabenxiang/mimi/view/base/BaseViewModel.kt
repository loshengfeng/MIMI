package com.dabenxiang.mimi.view.base

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dabenxiang.mimi.manager.AccountManager
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.pref.Pref
import com.google.gson.Gson
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val app: Application by inject()
    val accountManager: AccountManager by inject()
    val gson: Gson by inject()
    val domainManager: DomainManager by inject()
    val pref: Pref by inject()

    val toastData = MutableLiveData<String>()

    private val _showProgress by lazy { MutableLiveData<Boolean>() }
    val showProgress: LiveData<Boolean> get() = _showProgress

    fun setShowProgress(show: Boolean) {
        _showProgress.value = show
    }

    fun processException(exceptionResult: ExceptionResult) {
        // todo: send log but API haven't ready...
//        when (exceptionResult) {
//            is ExceptionResult.Crash -> deviceManager.sendCrashReport(AppUtils.getExceptionDetail(exceptionResult.throwable))
//            is ExceptionResult.HttpError -> deviceManager.sendCrashReport(AppUtils.getExceptionDetail(exceptionResult.httpExceptionItem.httpExceptionClone))
//        }
    }

    fun logoutLocal() {
        accountManager.logoutLocal()
    }

    /**
     * 取得要分享的 URL
     * @param categoryURI 分類tab, e.g: 首頁、電視劇、綜藝、etc.
     * @param videoId 該影片 ID
     * @param episodeID 該級數 ID, 若為 null or -1 不帶, 反之加上 "&e="
     */
    fun getShareUrl(categoryURI: String, videoId: Long, episodeID: String? = null): String {
        val result: StringBuilder = StringBuilder()
        result.append(domainManager.getApiDomain())
            .append("/play/")
            .append(Uri.encode(categoryURI))
            .append("?u=")
            .append(videoId)
        if (episodeID != null && episodeID != "-1") {
            result.append("&e=")
                .append(episodeID)
        }
        return result.toString()
    }
}
