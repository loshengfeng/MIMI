package com.dabenxiang.mimi.view.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MediaContentItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ClipPlayerViewModel: BaseViewModel() {

    private val _memberPostContentSource = MutableLiveData<ApiResult<MemberPostItem>>()
    val memberPostContentSource: LiveData<ApiResult<MemberPostItem>> = _memberPostContentSource

    private val _videoStreamingUrl = MutableLiveData<String>()
    val videoStreamingUrl: LiveData<String> = _videoStreamingUrl

    var videoContentId : Long = -1
    var m3u8SourceUrl: String = ""

    fun getPostDetail() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMemberPostDetail(videoContentId)
                if(!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.d(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect{
                    _memberPostContentSource.value = it
                }
        }
    }

    fun parsingM3u8Source(item: MediaContentItem) {
        _videoStreamingUrl.value = item.shortVideo?.url
    }

}