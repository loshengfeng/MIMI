package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
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

    fun deleteMIMIVideoFavorite(type: MyPagesType, videoId : String){
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.deleteMePlaylist(videoId)
                Timber.i("$type deleteMIMIVideoFavorite result= $result")
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.isSuccessful))
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion {
                        mimiDB.postDBItemDao().getMemberPostItemByVideoId(videoId.toLong())?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                Timber.i("$type deleteMIMIVideoFavorite item= $this")
                                this.isFavorite = false
                                this.favoriteCount = this.favoriteCount-1
                            }
                            val pageCode = MyPagesPostMediator::class.simpleName + type.toString()
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                            mimiDB.postDBItemDao().deleteItemByPageCode(
                                    pageCode= MyPagesPostMediator::class.simpleName + type.toString(),
                                    postDBId = memberPostItem.id
                            )
                            mimiDB.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                        }
                    }
                    .collect {
                        Timber.i("deleteMIMIVideoFavorite = $it")
                        _deleteFavoriteResult.value = it }
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