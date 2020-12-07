package com.dabenxiang.mimi.view.search.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.search.post.paging.SearchPostFollowDataSource
import com.dabenxiang.mimi.view.search.video.paging.SearchVideoListDataSource
import com.dabenxiang.mimi.widget.utility.EditTextLiveData
import com.dabenxiang.mimi.widget.utility.EditTextMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchVideoViewModel : BaseViewModel() {

    var category = ""
    var searchingTag = ""
    var searchingStr = ""

    var currentItem: VideoItem? = null

    private val _searchingTotalCount = MutableLiveData<Long>()
    val searchingTotalCount: LiveData<Long> = _searchingTotalCount

    private val _likeResult = MutableLiveData<ApiResult<Int>>()
    val likeResult: LiveData<ApiResult<Int>> = _likeResult

    private val _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private val pagingCallback = object : SearchPagingCallback {
        override fun onTotalCount(count: Long) {
            _searchingTotalCount.postValue(count)
        }

        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
        }
    }

    fun getSearchVideoResult(
        keyword: String? = null,
        tag: String? = null
    ): Flow<PagingData<VideoItem>> {
        return Pager(
            config = PagingConfig(pageSize = SearchVideoListDataSource.PER_LIMIT.toInt()),
            pagingSourceFactory = {
                SearchVideoListDataSource(
                    domainManager,
                    pagingCallback,
                    category,
                    tag,
                    keyword,
                    adWidth,
                    adHeight
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    fun modifyLike(videoID: Long, position: Int) {
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
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likeResult.value = it }
        }
    }

    fun modifyFavorite(videoID: Long, position: Int) {
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
                    favoriteCount =
                        if (favorite) (favoriteCount ?: 0) + 1
                        else (favoriteCount ?: 0) - 1
                }
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun getSearchHistory(): ArrayList<String> {
        return pref.searchHistoryItem.searchHistory
    }

    fun clearSearchHistory() {
        pref.searchHistoryItem = SearchHistoryItem()
    }

    fun updateSearchHistory(keyword: String) {
        val searchHistoryItem = pref.searchHistoryItem
        if (!searchHistoryItem.searchHistory.contains(keyword)) {
            if (searchHistoryItem.searchHistory.size == 10) {
                searchHistoryItem.searchHistory.removeAt(0)
                searchHistoryItem.searchHistory.add(keyword)
            } else {
                searchHistoryItem.searchHistory.add(keyword)
            }
            pref.searchHistoryItem = searchHistoryItem
        }
    }
}