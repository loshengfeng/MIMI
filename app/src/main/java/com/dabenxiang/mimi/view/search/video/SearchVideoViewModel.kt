package com.dabenxiang.mimi.view.search.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.search.post.SearchPostMediator
import com.dabenxiang.mimi.view.search.video.paging.SearchVideoDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchVideoViewModel : BaseViewModel() {

    var category = ""
    var searchingTag = ""
    var searchingStr = ""
    var videoType: VideoType? = null

    private val _searchingTotalCount = MutableLiveData<Long>()
    val searchingTotalCount: LiveData<Long> = _searchingTotalCount

    private val _likeResult = MutableLiveData<ApiResult<Int>>()
    val likeResult: LiveData<ApiResult<Int>> = _likeResult

    private val _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    val pageCode = SearchVideoFragment::class.simpleName + ""

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

    fun modifyLike(item: MemberPostItem, position: Int) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                if (item.likeType != LikeType.LIKE) {
                    val request = LikeRequest(LikeType.LIKE)
                    val result = apiRepository.like(item.id, request)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = LikeType.LIKE
                    item.likeCount += 1
                } else {
                    val result = apiRepository.deleteLike(item.id)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = null
                    item.likeCount -= 1
                }
                changeLikeMimiVideoInDb(item.id, item.likeType)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likeResult.value = it }
        }
    }

    fun modifyFavorite(item:MemberPostItem, position: Int) {
        viewModelScope.launch {
            flow {
                val result = if (!item.isFavorite) {
                    domainManager.getApiRepository().postMePlaylist(PlayListRequest(item.id, 1))
                } else {
                    domainManager.getApiRepository().deleteMePlaylist(item.id.toString())
                }
                if (!result.isSuccessful) throw HttpException(result)
                changeFavoriteMimiVideoInDb(item.id)
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

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(
        keyword: String?,
        tag: String?,
        videoType: VideoType?
    ) = clearResult()
        .flatMapConcat { postItems(keyword, tag, videoType) }.cachedIn(viewModelScope)

    private fun postItems(
        keyword: String?,
        tag: String?,
        videoType: VideoType?
    ) = Pager(
        config = PagingConfig(pageSize = SearchVideoMediator.PER_LIMIT),
        remoteMediator = SearchVideoMediator(
            mimiDB,
            domainManager,
            pagingCallback,
            pageCode,
            category,
            tag,
            keyword,
            adWidth,
            adHeight,
            videoType
        )
    ) {
        mimiDB.postDBItemDao()
            .pagingSourceByPageCode(pageCode)
    }.flow.map { pagingData ->
        pagingData.map {
            it.memberPostItem
        }
    }

    private fun clearResult(): Flow<Nothing?> {
        return flow {
            mimiDB.postDBItemDao().deleteItemByPageCode(pageCode)
            mimiDB.remoteKeyDao().deleteByPageCode(pageCode)
            emit(null)
        }
    }
}