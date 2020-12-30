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
import com.dabenxiang.mimi.view.my_pages.base.MyPagesPostMediator
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber

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

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }
    }

    fun favorite(item: PlayItem, position: Int, type:MyPagesType) {
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
                item.favorite = !originFavorite
                item.favoriteCount =
                    if (originFavorite) originFavoriteCnt - 1
                    else originFavoriteCnt + 1
                _videoChangedResult.postValue(
                    ApiResult.success(
                        VideoItem(
                            id = item.videoId?:0,
                            favorite = item.favorite ?: false,
                            favoriteCount = item.favoriteCount?:0,
                            like = item.like,
                            likeType = if(item.like==true) LikeType.LIKE else if(item.like==false) LikeType.DISLIKE else null,
                            likeCount = item.likeCount?:0
                        )
                    )
                )
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion {
                    item.videoId?.let { videoId->
                        mimiDB.postDBItemDao().getMemberPostItemByVideoId(videoId)?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                Timber.i("$type favorite item= $this")
                                this.isFavorite = false
                                this.favoriteCount = this.favoriteCount-1
                            }
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                        }
                    }

                }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun like(item: VideoItem, likeType: LikeType, type:MyPagesType) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion {
                    item.id?.let { id->
                        mimiDB.postDBItemDao().getMemberPostItemById(id)?.let { memberPostItem->
                            val item = memberPostItem.apply {
                                Timber.i("$type like item= $this")
                                when(likeType) {
                                    LikeType.LIKE -> {
                                        this.likeType = LikeType.LIKE
                                        this.likeCount += 1
                                    }
                                    else-> {
                                        this.likeType = LikeType.DISLIKE
                                        this.likeCount -= 1
                                    }
                                }
                            }
                            mimiDB.postDBItemDao().insertMemberPostItem(item)
                            mimiDB.postDBItemDao().deleteItemByPageCode(
                                    pageCode= MyPagesPostMediator::class.simpleName + type.toString(),
                                    postDBId = memberPostItem.id
                            )
                        }
                    }
                }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likeResult.value = it }
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