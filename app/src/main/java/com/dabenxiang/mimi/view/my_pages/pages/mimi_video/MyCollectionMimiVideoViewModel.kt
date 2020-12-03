package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MyCollectionMimiVideoViewModel : ClubViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    private var _videoFavoriteResult = MutableLiveData<ApiResult<Int>>()
    val videoFavoriteResult: LiveData<ApiResult<Int>> = _videoFavoriteResult

    private val _deleteFavoriteResult = MutableLiveData<ApiResult<Boolean>>()
    val deleteFavoriteResult: LiveData<ApiResult<Boolean>> = _deleteFavoriteResult

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    var totalCount: Int = 0

    fun getData(adapter: MyCollectionMimiVideoAdapter, type: MyCollectionTabItemType, isLike: Boolean) {
        Timber.i("getData")
        CoroutineScope(Dispatchers.IO).launch {
            adapter.submitData(PagingData.empty())
            if(isLike) {
                getLikeItemList()
                    .collect {
                        adapter.submitData(it)
                    }
            } else {
                getPostItemList(type)
                    .collectLatest {
                        adapter.submitData(it)
                    }
            }
        }
    }

    fun getPostItemList(type: MyCollectionTabItemType): Flow<PagingData<PlayItem>> {
        return Pager(
            config = PagingConfig(pageSize = MyCollectionMimiVideoDataSource.PER_LIMIT),
            pagingSourceFactory = {
                MyCollectionMimiVideoDataSource(
                    domainManager,
                    pagingCallback,
                    adWidth,
                    adHeight,
                    type
                )
            }
        )
            .flow
            .onStart {  setShowProgress(true) }
            .onCompletion { setShowProgress(false) }
            .cachedIn(viewModelScope)
    }

    fun getLikeItemList(): Flow<PagingData<PlayItem>> {
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
            .onStart {  setShowProgress(true) }
            .onCompletion { setShowProgress(false) }
            .cachedIn(viewModelScope)
    }

    fun deleteMIMIVideoFavorite(videoId : String){
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

    fun modifyFavorite(item: VideoItem, position: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val resp = when {
                    isFavorite -> apiRepository.postMePlaylist(PlayListRequest(item.id, 1))
                    else -> apiRepository.deleteMePlaylist(item.id.toString())
                }
                if (!resp.isSuccessful) throw HttpException(resp)
                item.favorite = isFavorite
                item.favoriteCount = item.favoriteCount?.let { if (isFavorite) it + 1 else it - 1 }
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videoFavoriteResult.value = it }
        }
    }

    fun deleteVideos(items: List<PlayItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                        .deleteMePlaylist(
                                items.map {it.videoId}.joinToString(separator = ",")
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

    fun deleteAllLike(items: List<PlayItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deleteAllLike(
                        items.map {it.videoId}.joinToString(separator = ",")
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