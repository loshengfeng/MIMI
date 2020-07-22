package com.dabenxiang.mimi.view.search.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.SearchVideoPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.EditTextLiveData
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchVideoViewModel : BaseViewModel() {

    var searchingTag = ""
    var searchingStr = ""
    var isAdult = false

    var currentItem: VideoItem? = null

    private val _searchTextLiveData = EditTextMutableLiveData()
    val searchTextLiveData: EditTextLiveData = _searchTextLiveData

    private val _searchingListResult = MutableLiveData<PagedList<VideoItem>>()
    val searchingListResult: LiveData<PagedList<VideoItem>> = _searchingListResult

    private val _searchingTotalCount = MutableLiveData<Long>()
    val searchingTotalCount: LiveData<Long> = _searchingTotalCount

    private val _likeResult = MutableLiveData<ApiResult<Long>>()
    val likeResult: LiveData<ApiResult<Long>> = _likeResult

    private val _favoriteResult = MutableLiveData<ApiResult<Long>>()
    val favoriteResult: LiveData<ApiResult<Long>> = _favoriteResult

    private val _postReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postReportResult: LiveData<ApiResult<Nothing>> = _postReportResult

    private fun getVideoPagingItems(
            isAdult: Boolean
    ): LiveData<PagedList<VideoItem>> {
        val searchVideoDataSource =
            SearchVideoListDataSource(
                viewModelScope,
                domainManager,
                domainManager.getApiRepository(),
                pagingCallback,
                isAdult,
                searchingTag,
                searchingStr
            )
        val videoFactory =
            SearchVideoFactory(
                searchVideoDataSource
            )
        val config = PagedList.Config.Builder()
                .setPrefetchDistance(4)
                .build()
        return LivePagedListBuilder(videoFactory, config).build()
    }

    private val pagingCallback = object : SearchVideoPagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
        }

        override fun onTotalCount(count: Long) {
            _searchingTotalCount.postValue(count)
        }
    }

    fun cleanSearchText() {
        _searchTextLiveData.value = ""
    }

    fun getSearchList() {
        viewModelScope.launch {
            getVideoPagingItems(isAdult).asFlow()
                    .collect {
                        _searchingListResult.value = it
                    }
        }
    }

    fun modifyLike(videoID: Long) {
        val likeType = if (currentItem?.like == true) LikeType.DISLIKE else LikeType.LIKE
        val likeRequest = LikeRequest(likeType)
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                        .like(videoID, likeRequest)
                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                currentItem?.run {
                    like = like != true
                    likeCount = if (like == true) (likeCount ?: 0) + 1 else (likeCount ?: 0) - 1
                }
                emit(ApiResult.success(videoID))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .collect { _likeResult.value = it }
        }
    }

    fun modifyFavorite(videoID: Long) {
        viewModelScope.launch {
            flow {
                val result = if (currentItem?.favorite == false) {
                    domainManager.getApiRepository().postMePlaylist(PlayListRequest(videoID, 1))
                } else {
                    domainManager.getApiRepository().deleteMePlaylist(videoID.toString())
                }

                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                currentItem?.run {
                    favorite = favorite != true
                    favoriteCount = if (favorite == true) (favoriteCount
                            ?: 0) + 1 else (favoriteCount ?: 0) - 1
                }
                emit(ApiResult.success(videoID))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .collect { _favoriteResult.value = it }
        }
    }

    fun sendPostReport(item: MemberPostItem, content: String) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val result = domainManager.getApiRepository().sendPostReport(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                item.reported = true
                emit(ApiResult.success(null))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _postReportResult.value = it }
        }
    }
}