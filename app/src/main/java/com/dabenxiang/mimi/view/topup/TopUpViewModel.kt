package com.dabenxiang.mimi.view.topup

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.favroite.FavoritePlayListDataSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TopUpViewModel : BaseViewModel() {

    private val _agentList = MutableLiveData<PagedList<Any>>()
    val agentList: LiveData<PagedList<Any>> = _agentList

    private val _meItem = MutableLiveData<ApiResult<MeItem>>()
    val meItem: LiveData<ApiResult<MeItem>> = _meItem

    private val _avatar = MutableLiveData<ApiResult<Bitmap>>()
    val avatar: LiveData<ApiResult<Bitmap>> = _avatar

    fun initData() {
        viewModelScope.launch {
            val dataSrc = TopUpProxyPayListDataSource(
                viewModelScope,
                domainManager,
                topUpPagingCallback
            )
            dataSrc.isInvalid
            val factory = TopUpProxyPayListFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(FavoritePlayListDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow()
                .collect { _agentList.postValue(it) }
        }
    }

    fun getUserData(): ProfileItem {
        return accountManager.getProfile()
    }

    fun getMe() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                if (!result.isSuccessful) throw HttpException(result)
                val meItem = result.body()?.content
                meItem?.let {
                    accountManager.setupProfile(it)
                    getAttachment(it.avatarAttachmentId!!)
                }
                emit(ApiResult.success(meItem))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _meItem.value = it }
        }
    }

    fun getAttachment(id: Long) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getAttachment(id.toString())
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                emit(ApiResult.success(bitmap))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _avatar.value = it }
        }
    }

    private val topUpPagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}
    }
}