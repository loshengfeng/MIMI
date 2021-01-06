package com.dabenxiang.mimi.view.search.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.search.video.paging.SearchVideoDataSource
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchVideoViewModel : BaseViewModel() {

    var category = ""
    var searchingTag = ""
    var searchingStr = ""
    var videoType: VideoType? = null

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
        tag: String? = null,
        videoType: VideoType? =null
    ): Flow<PagingData<VideoItem>> {
        return Pager(
            config = PagingConfig(pageSize = SearchVideoDataSource.PER_LIMIT),
            pagingSourceFactory = {
                SearchVideoDataSource(
                    domainManager,
                    pagingCallback,
                    category,
                    tag,
                    keyword,
                    adWidth,
                    adHeight,
                    videoType
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    fun modifyLike(position: Int) {
        viewModelScope.launch {
            flow {
                currentItem?.also { item ->
                    val apiRepository = domainManager.getApiRepository()
                    val likeType = when (item.like) {
                        true -> null
                        else -> LikeType.LIKE
                    }
                    val request =  LikeRequest(likeType)
                    val result = when (item.like) {
                        true -> apiRepository.deleteLike(item.id)
                        else -> apiRepository.like(item.id, request)
                    }
                    if (!result.isSuccessful) throw HttpException(result)
                    when (item.like) {
                        true -> item.likeCount -= 1
                        else -> item.likeCount += 1
                    }
                    item.likeType = likeType
                    item.like = item.like != true
                    emit(ApiResult.success(position))
                }
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
                val body = result.body()?.content
                val countItem = when (currentItem?.favorite) {
                    false -> body
                    else -> (body as ArrayList<*>)[0]
                }
                countItem as InteractiveHistoryItem

                currentItem?.run {
                    favorite = favorite != true
                    favoriteCount = countItem.favoriteCount
                }

                LruCacheUtils.putShortVideoDataCache(
                    videoID,
                    PlayItem(
                        favorite = currentItem?.favorite,
                        favoriteCount = countItem.favoriteCount?.toInt(),
                        commentCount = countItem.commentCount?.toInt()
                    )
                )

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