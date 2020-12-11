package com.dabenxiang.mimi.view.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.GuessLikePagingCallBack
import com.dabenxiang.mimi.event.SingleLiveEvent
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.vo.BaseVideoItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.player.GuessLikeDataSource
import com.dabenxiang.mimi.view.player.GuessLikeFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class PlayerDescriptionViewModel : BaseViewModel() {

    private val _getAdResult = MutableLiveData<ApiResult<AdItem>>()
    val getAdResult: LiveData<ApiResult<AdItem>> = _getAdResult

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private var _likeResult = MutableLiveData<ApiResult<VideoItem>>()
    val likeResult: LiveData<ApiResult<VideoItem>> = _likeResult

    private var _favoriteResult = MutableLiveData<ApiResult<VideoItem>>()
    val favoriteResult: LiveData<ApiResult<VideoItem>> = _favoriteResult

    private val _reportResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val reportResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _reportResult

    fun getAd(width: Int, height: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getAdRepository().getAD(width, height)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _getAdResult.value = it }
        }
    }

    fun setupGuessLikeList(
        tags: String?,
        performers: String?,
        isAdult: Boolean,
        videoContentId: Long
    ) {
        viewModelScope.launch {
            val dataSrc = GuessLikeDataSource(
                isAdult,
                tags ?: "",
                performers ?: "",
                viewModelScope,
                domainManager.getApiRepository(),
                pagingCallback,
                videoContentId
            )
            val factory = GuessLikeFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(GuessLikeDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    fun favorite(item: VideoItem) {
        viewModelScope.launch {
            flow {
                val originFavorite = item.favorite
                val originFavoriteCnt = item.favoriteCount ?: 0
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    !originFavorite -> apiRepository.postMePlaylist(PlayListRequest(item.id, 1))
                    else -> apiRepository.deleteMePlaylist(item.id.toString())
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.favorite = !originFavorite
                item.favoriteCount = if (originFavorite) originFavoriteCnt - 1
                else originFavoriteCnt + 1
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _videoChangedResult.value = it
                    _favoriteResult.value = it
                }
        }
    }

    fun like(item: VideoItem, type: LikeType) {
        viewModelScope.launch {
            flow {
                val originType = item.likeType
                val apiRepository = domainManager.getApiRepository()
                if (type != originType) {
                    val request = LikeRequest(type)
                    val result = apiRepository.like(item.id, request)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = type
                    if (type == LikeType.LIKE) {
                        if (originType == LikeType.DISLIKE) item.dislikeCount -= 1
                        item.likeCount += 1
                    } else {
                        if (originType == LikeType.LIKE) item.likeCount -= 1
                        item.dislikeCount += 1
                    }
                } else {
                    val result = apiRepository.deleteLike(item.id)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = null
                    if (type == LikeType.LIKE) item.likeCount -= 1
                    else item.dislikeCount -= 1
                }
                item.like = if (item.likeType == null) null
                else item.likeType == LikeType.LIKE
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _videoChangedResult.value = it
                    _likeResult.value = it
                }
        }
    }

    fun report(id: Long, content: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().sendVideoReport(
                    ReportRequest(content, id)
                )
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    Timber.i("sentReport =$it")
                    _reportResult.value = SingleLiveEvent(it)
                }
        }
    }

    private val pagingCallback = object : GuessLikePagingCallBack {
        override fun onLoadInit(initCount: Int) {
        }

        override fun onLoading() {
        }

        override fun onLoaded() {
        }

        override fun onThrowable(throwable: Throwable) {
        }
    }


}