package com.dabenxiang.mimi.view.my_pages.pages.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber

class LikeMimiVideoViewModel : ClubViewModel() {

    private val _deleteFavoriteResult = MutableLiveData<ApiResult<Boolean>>()
    val deleteFavoriteResult: LiveData<ApiResult<Boolean>> = _deleteFavoriteResult

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    var totalCount: Int = 0

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(type: MyPagesType) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(type)

    ).flattenMerge(2).cachedIn(viewModelScope)

    private fun postItems(type: MyPagesType) = Pager(
            config = PagingConfig(pageSize = MyPagesPostMediator.PER_LIMIT),
            remoteMediator = MyPagesPostMediator(mimiDB, domainManager, type, pagingCallback)
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode(MyPagesPostMediator::class.simpleName + type.toString())


    }.flow.map { pagingData->
        pagingData.map { dbItem->
            dbItem.memberPostItem.toPlayItem()
        }
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

    fun favorite(item: PlayItem, position: Int, type:MyPagesType, isFavorite:Boolean) {
        viewModelScope.launch {
            flow {
                val originFavorite = item.favorite ?: false
                val originFavoriteCnt = item.favoriteCount ?: 0
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.postMePlaylist(
                        PlayListRequest(
                            item.videoId,
                            1
                        )
                    )
                    else -> apiRepository.deleteMePlaylist(item.videoId.toString())
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.favorite = !originFavorite
                item.favoriteCount =
                    if (originFavorite) originFavoriteCnt - 1
                    else originFavoriteCnt + 1
                when(type){
                    MyPagesType.LIKE_MIMI -> changeFavoriteMimiVideoInDb(item.videoId?:0)
                    MyPagesType.LIKE_SHORT_VIDEO -> changeFavoriteSmallVideoInDb(item.videoId?:0)
                }
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion {emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { }
        }
    }

    fun deleteAllLike(type:MyPagesType, items: List<PlayItem>) {
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
                .onCompletion {
                    val pageCode =MyPagesPostMediator::class.simpleName + type.toString()
                    mimiDB.postDBItemDao().deleteItemByPageCode(
                            pageCode= pageCode
                    )
                    mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                }
                .collect { _cleanResult.value = it }
        }
    }
}