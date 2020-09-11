package com.dabenxiang.mimi.view.base

import android.app.Application
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.PROJECT_NAME
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getExceptionDetail
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.HttpException
import tw.gov.president.manager.submanager.logmoniter.di.SendLogManager
import java.util.*

abstract class BaseViewModel : ViewModel(), KoinComponent {

    val app: Application by inject()
    val gson: Gson by inject()
    val pref: Pref by inject()
    val accountManager: AccountManager by inject()
    val domainManager: DomainManager by inject()
    val mqttManager: MQTTManager by inject()

    private val _showProgress by lazy { MutableLiveData<Boolean>() }
    val showProgress: LiveData<Boolean> get() = _showProgress

    fun setShowProgress(show: Boolean) {
        _showProgress.value = show
    }

    fun processException(exceptionResult: ExceptionResult) {
        when (exceptionResult) {
            is ExceptionResult.Crash -> {
                sendCrashReport(getExceptionDetail(exceptionResult.throwable))
            }
            is ExceptionResult.HttpError -> {
                sendCrashReport(getExceptionDetail(exceptionResult.httpExceptionItem.httpExceptionClone))
            }
        }
    }

    fun isLogin(): Boolean {
        return accountManager.isLogin()
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

    fun sendCrashReport(data: String) {
        SendLogManager.e(PROJECT_NAME, data)
    }

    private var _deletePostResult = MutableLiveData<ApiResult<Int>>()
    val deletePostResult: LiveData<ApiResult<Int>> = _deletePostResult

    private val _cleanRemovedPosList = MutableLiveData<Nothing>()
    val cleanRemovedPosList: LiveData<Nothing> = _cleanRemovedPosList

    fun deletePost(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMyPost(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                val position = items.indexOf(item)
                items.remove(item)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _deletePostResult.value = it }
        }
    }

    fun cleanRemovedPosList() {
        _cleanRemovedPosList.value = null
    }

    fun loadImage(id: Long?, view: ImageView, type: LoadImageType) {
        val defaultResId = when (type) {
            LoadImageType.AVATAR -> R.drawable.default_profile_picture
            LoadImageType.AVATAR_CS -> R.drawable.icon_cs_photo
            LoadImageType.PICTURE_THUMBNAIL -> R.drawable.img_nopic_03
            LoadImageType.PICTURE_FULL -> R.drawable.img_nopic_03
            LoadImageType.CLUB -> R.drawable.ico_group
            LoadImageType.CHAT_CONTENT -> R.drawable.bg_gray_6_radius_16
        }
        if (id == null || id == 0L) {
            Glide.with(view.context).load(defaultResId).into(view)
        } else {
            val accessToken =
                if (accountManager.isLogin()) pref.memberToken.accessToken else pref.publicToken.accessToken
            val auth = StringBuilder(ApiRepository.BEARER).append(accessToken).toString()
            val url = "${domainManager.getApiDomain()}/v1/Attachments/$id"
            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder().addHeader(ApiRepository.AUTHORIZATION, auth).build()
            )
            val options = RequestOptions()
                .priority(Priority.NORMAL)
                .placeholder(defaultResId)
                .error(defaultResId)
            when (type) {
                LoadImageType.AVATAR,
                LoadImageType.AVATAR_CS,
                LoadImageType.CLUB -> {
                    options.transform(MultiTransformation(CenterCrop(), CircleCrop()))
                }
                LoadImageType.PICTURE_THUMBNAIL -> {
                    options.transform(MultiTransformation(CenterCrop()))
                }
                LoadImageType.PICTURE_FULL -> {
                }
                LoadImageType.CHAT_CONTENT -> {
                    options.transform(CenterCrop(), RoundedCorners(16))
                }
            }
            Glide.with(view.context).load(glideUrl)
                .apply(options)
                .into(view)
        }
    }
}