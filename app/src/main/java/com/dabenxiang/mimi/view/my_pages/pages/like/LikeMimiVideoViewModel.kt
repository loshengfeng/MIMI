package com.dabenxiang.mimi.view.my_pages.pages.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.my_pages.pages.mimi_video.MyCollectionMimiVideoDataSource
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LikeMimiVideoViewModel : BaseViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    private val _deleteFavoriteResult = MutableLiveData<ApiResult<Boolean>>()
    val deleteFavoriteResult: LiveData<ApiResult<Boolean>> = _deleteFavoriteResult

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private var _likeResult = MutableLiveData<ApiResult<VideoItem>>()
    val likeResult: LiveData<ApiResult<VideoItem>> = _likeResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    var totalCount: Int = 0

    fun getData(adapter: LikeMimiVideoAdapter) {
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            getLikeItemList()
                .collect {
                    adapter.submitData(it)
                }
        }
    }

    private fun getLikeItemList(): Flow<PagingData<PlayItem>> {
        return Pager(
            config = PagingConfig(pageSize = MyCollectionMimiVideoDataSource.PER_LIMIT),
            pagingSourceFactory = {
                MiMiLikeListDataSource(
                    domainManager,
                    pagingCallback,
                )
            }
        )
            .flow
            .onStart { setShowProgress(true) }
            .onCompletion { setShowProgress(false) }
            .cachedIn(viewModelScope)
    }

    fun deleteMIMIVideoFavorite(videoId: String) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMePlaylist(videoId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.isSuccessful))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _deleteFavoriteResult.value = it }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }
    }

    fun favorite(item: PlayItem, position: Int) {
        viewModelScope.launch {
            flow {
                val originFavorite = item.favorite ?: false
                val originFavoriteCnt = item.favoriteCount ?: 0
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    !originFavorite -> apiRepository.postMePlaylist(
                        PlayListRequest(
                            item.videoId,
                            1
                        )
                    )
                    else -> apiRepository.deleteMePlaylist(item.videoId.toString())
                }
                if (!result.isSuccessful) throw HttpException(result)
                val body = result.body()?.content
                val countItem = when {
                    !originFavorite -> body
                    else -> (body as ArrayList<*>)[0]
                }
                countItem as CountItem
                item.favorite = !originFavorite
                item.favoriteCount = countItem.favoriteCount.toInt()
                LruCacheUtils.putShortVideoDataCache(item.id, item)
                _videoChangedResult.postValue(
                    ApiResult.success(
                        VideoItem(
                            id = item.videoId?:0,
                            favorite = item.favorite ?: false,
                            favoriteCount = item.favoriteCount?.toLong(),
                            like = item.like,
                            likeType = if(item.like==true) LikeType.LIKE else if(item.like==false) LikeType.DISLIKE else null,
                            likeCount = item.likeCount?.toLong()?:0
                        )
                    )
                )
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun like(item: VideoItem, type: LikeType) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val request = LikeRequest(type)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likeResult.value = it }
        }
    }

    fun deleteAllLike(items: List<PlayItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deleteAllLike(
                        items.map { it.videoId }.joinToString(separator = ",")
                    )
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _cleanResult.value = it }
        }
    }
}