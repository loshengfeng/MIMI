package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.InteractiveHistoryItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException
import timber.log.Timber

class MyCollectionMimiVideoViewModel : ClubViewModel() {

    private var _videoFavoriteResult = MutableLiveData<ApiResult<Int>>()
    val videoFavoriteResult: LiveData<ApiResult<Int>> = _videoFavoriteResult

    private val _deleteFavoriteResult = MutableLiveData<ApiResult<Boolean>>()
    val deleteFavoriteResult: LiveData<ApiResult<Boolean>> = _deleteFavoriteResult

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    var totalCount: Int = 0

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(pageCode: String, type: MyPagesType) =
        postItems(pageCode, type).cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    private fun postItems(pageCode: String, type: MyPagesType) = Pager(
        config = PagingConfig(pageSize = MyPagesPostMediator.PER_LIMIT),
        remoteMediator = MyPagesPostMediator(mimiDB, domainManager, type, pageCode, pagingCallback)
    ) {
        mimiDB.postDBItemDao()
            .pagingSourceByPageCode(pageCode)
    }.flow.map { pagingData ->
        pagingData.map { dbItem ->
            dbItem.memberPostItem.toPlayItem()
        }
    }

    fun deleteVideoFavorite(type: MyPagesType, videoId: String) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMePlaylist(videoId)
                if (!result.isSuccessful) throw HttpException(result)
                val body = result.body()?.content
                val countItem = (body as ArrayList<*>)[0] as InteractiveHistoryItem
                when (type) {
                    MyPagesType.FAVORITE_MIMI_VIDEO -> changeFavoriteMimiVideoInDb(videoId.toLong())
                    MyPagesType.FAVORITE_SHORT_VIDEO -> {
                        LruCacheUtils.putShortVideoDataCache(
                            videoId.toLong(),
                            PlayItem(
                                favorite = false,
                                favoriteCount = countItem.favoriteCount?.toInt(),
                                commentCount = countItem.commentCount?.toInt()
                            )
                        )
                        changeFavoriteSmallVideoInDb(videoId.toLong())
                    }
                }
                emit(ApiResult.success(result.isSuccessful))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _deleteFavoriteResult.value = it
                }
        }
    }

    fun deleteVideos(type: MyPagesType, items: List<PlayItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deleteMePlaylist(
                        items.map { it.videoId }.joinToString(separator = ",")
                    )
                if (!result.isSuccessful) throw HttpException(result)
                val body = result.body()?.content
                val countItems = (body as ArrayList<*>)
                if(type == MyPagesType.FAVORITE_SHORT_VIDEO){
                    items.forEachWithIndex { i, playItem ->
                        takeIf { i < countItems.size }?.let { countItems[i] }?.let { item ->
                            item as InteractiveHistoryItem
                            playItem.videoId?.let { videoId ->
                                LruCacheUtils.putShortVideoDataCache(
                                    videoId,
                                    PlayItem(
                                        favorite = false,
                                        favoriteCount = item.favoriteCount?.toInt(),
                                        commentCount = item.commentCount?.toInt()
                                    )
                                )
                            }
                        }
                    }
                }
                items.map { it.videoId }.filterNotNull().forEach { videoId ->
                    when (type) {
                        MyPagesType.FAVORITE_MIMI_VIDEO -> changeFavoriteMimiVideoInDb(videoId)
                        MyPagesType.FAVORITE_SHORT_VIDEO -> changeFavoriteSmallVideoInDb(videoId)
                    }
                }

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