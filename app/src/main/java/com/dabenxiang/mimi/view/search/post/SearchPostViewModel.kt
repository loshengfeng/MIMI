package com.dabenxiang.mimi.view.search.post

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.club.ClubDataSource
import com.dabenxiang.mimi.view.home.club.ClubFactory
import com.dabenxiang.mimi.view.search.post.keyword.SearchPostByKeywordDataSource
import com.dabenxiang.mimi.view.search.post.keyword.SearchPostByKeywordFactory
import com.dabenxiang.mimi.view.search.post.tag.SearchPostByTagDataSource
import com.dabenxiang.mimi.view.search.post.tag.SearchPostByTagFactory
import com.dabenxiang.mimi.view.search.video.SearchVideoFactory
import com.dabenxiang.mimi.view.search.video.SearchVideoListDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchPostViewModel : BaseViewModel() {

    private val _clubItemListResult = MutableLiveData<PagedList<MemberClubItem>>()
    val clubItemListResult: LiveData<PagedList<MemberClubItem>> = _clubItemListResult

    private val _searchPostItemByTagListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val searchPostItemByTagListResult: LiveData<PagedList<MemberPostItem>> =
        _searchPostItemByTagListResult

    private val _searchPostItemByKeywordListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val searchPostItemByKeywordListResult: LiveData<PagedList<MemberPostItem>> =
        _searchPostItemByKeywordListResult

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private val _searchTotalCount = MutableLiveData<Long>()
    val searchTotalCount: LiveData<Long> = _searchTotalCount

    private val _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private val _searchingListResult = MutableLiveData<PagedList<VideoItem>>()
    val searchingListResult: LiveData<PagedList<VideoItem>> = _searchingListResult

    private val _likeVideoResult = MutableLiveData<ApiResult<Long>>()
    val likeVideoResult: LiveData<ApiResult<Long>> = _likeVideoResult

    private val _favoriteVideoResult = MutableLiveData<ApiResult<Long>>()
    val favoriteVideoResult: LiveData<ApiResult<Long>> = _favoriteVideoResult

    var currentVideoItem: VideoItem? = null

    fun followPost(item: MemberPostItem, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followPostResult.value = it }
        }
    }

    fun likePost(item: MemberPostItem, position: Int, isLike: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }

                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun likePost(item: MemberPostItem, isLike: Boolean, update: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }

                emit(ApiResult.success(item.likeCount))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(isLike, it.result)
                        }
                    }
                }
        }
    }

    fun getSearchPostsByTag(type: PostType, tag: String, isPostFollow: Boolean) {
        viewModelScope.launch {
            getSearchPostByTagPagingItems(type, tag, isPostFollow).asFlow()
                .collect { _searchPostItemByTagListResult.value = it }
        }
    }

    fun getSearchPostsByKeyword(type: PostType, keyword: String, isPostFollow: Boolean) {
        viewModelScope.launch {
            getSearchPostByKeywordPagingItems(type, keyword, isPostFollow).asFlow()
                .collect {
                    _searchPostItemByKeywordListResult.value = it
                }
        }
    }

    fun getSearchVideoList(searchingTag: String, searchingStr: String) {
        viewModelScope.launch {
            getVideoPagingItems(true, searchingTag, searchingStr).asFlow()
                    .collect {
                        _searchingListResult.value = it
                    }
        }
    }

    fun clubFollow(item: MemberClubItem, isFollow: Boolean, update: ((Boolean) -> Unit)) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followClub(item.id)
                    else -> apiRepository.cancelFollowClub(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(isFollow))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    fun isSearchTextEmpty(keyword: String): Boolean {
        return TextUtils.isEmpty(keyword)
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

    private fun getSearchPostByTagPagingItems(
        type: PostType,
        tag: String,
        isPostFollow: Boolean
    ): LiveData<PagedList<MemberPostItem>> {
        val dataSource = SearchPostByTagDataSource(
            pagingCallback,
            viewModelScope,
            domainManager,
            type,
            tag,
            isPostFollow,
            adWidth,
            adHeight
        )
        val factory = SearchPostByTagFactory(dataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(factory, config).build()
    }

    private fun getSearchPostByKeywordPagingItems(
        type: PostType,
        tag: String,
        isPostFollow: Boolean
    ): LiveData<PagedList<MemberPostItem>> {
        val dataSource = SearchPostByKeywordDataSource(
            pagingCallback,
            viewModelScope,
            domainManager,
            type,
            tag,
            isPostFollow,
            adWidth,
            adHeight
        )
        val factory = SearchPostByKeywordFactory(dataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(factory, config).build()
    }

    private fun getVideoPagingItems(
            isAdult: Boolean,
            searchingTag: String,
            searchingStr: String
    ): LiveData<PagedList<VideoItem>> {
        val searchVideoDataSource =
                SearchVideoListDataSource(
                    viewModelScope,
                    domainManager,
                    pagingCallback,
                    isAdult,
                    "",
                    searchingTag,
                    searchingStr,
                    adHeight,
                    adWidth
                )
        val videoFactory = SearchVideoFactory(searchVideoDataSource)
        val config = PagedList.Config.Builder()
                .setPrefetchDistance(4)
                .build()
        return LivePagedListBuilder(videoFactory, config).build()
    }

    fun getClubs(keyword: String) {
        viewModelScope.launch {
            getClubPagingItems(keyword).asFlow()
                .collect { _clubItemListResult.value = it }
        }
    }

    private fun getClubPagingItems(keyword: String): LiveData<PagedList<MemberClubItem>> {
        val clubDataSource =
            ClubDataSource(
                postPagingCallback,
                viewModelScope,
                domainManager,
                adWidth,
                adHeight,
                keyword
            )
        val clubFactory = ClubFactory(clubDataSource)
        val config = PagedList.Config.Builder().setPrefetchDistance(4).build()
        return LivePagedListBuilder(clubFactory, config).build()
    }

    var totalCount: Int = 0
    private val pagingCallback = object : SearchPagingCallback {
        override fun onTotalCount(count: Long) {
            _searchTotalCount.postValue(count)
        }

        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            setShowProgress(false)
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
            totalCount = if (isInitial) count.toInt()
            else totalCount.plus(count.toInt())
            if(isInitial) cleanRemovedPosList()
        }
    }

    private val postPagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _searchTotalCount.postValue(count)
        }

        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            setShowProgress(false)
        }
    }

    fun followMember(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>,
        isFollow: Boolean,
        update: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach {
                    if (it.creatorId == item.creatorId) {
                        it.isFollow = isFollow
                    }
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

    fun favoritePost(item: MemberPostItem, isFavorite: Boolean, update: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFavorite = isFavorite
                if (isFavorite) item.favoriteCount++ else item.favoriteCount--
                emit(ApiResult.success(item.favoriteCount))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(isFavorite, it.result)
                        }
                    }
                }
        }
    }

    fun modifyVideoLike(videoID: Long) {
        val likeType = if (currentVideoItem?.like == true) LikeType.DISLIKE else LikeType.LIKE
        val likeRequest = LikeRequest(likeType)
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                        .like(videoID, likeRequest)
                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                currentVideoItem?.run {
                    like = like != true
                    likeCount = if (like == true) (likeCount ?: 0) + 1 else (likeCount ?: 0) - 1
                }
                emit(ApiResult.success(videoID))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .collect { _likeVideoResult.value = it }
        }
    }

    fun modifyVideoFavorite(videoID: Long) {
        viewModelScope.launch {
            flow {
                val result = if (currentVideoItem?.favorite == false) {
                    domainManager.getApiRepository().postMePlaylist(PlayListRequest(videoID, 1))
                } else {
                    domainManager.getApiRepository().deleteMePlaylist(videoID.toString())
                }

                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                currentVideoItem?.run {
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
                    .collect { _favoriteVideoResult.value = it }
        }
    }
}