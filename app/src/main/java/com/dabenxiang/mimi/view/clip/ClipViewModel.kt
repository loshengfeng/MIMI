package com.dabenxiang.mimi.view.clip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClipViewModel : BaseViewModel() {

    private var _followResult = MutableLiveData<ApiResult<Int>>()
    val followResult: LiveData<ApiResult<Int>> = _followResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private val _videoReport = MutableLiveData<ApiResult<Nothing>>()
    val videoReport: LiveData<ApiResult<Nothing>> = _videoReport

    private val _rechargeVipResult = MutableLiveData<Nothing>()
    val rechargeVipResult: LiveData<Nothing> = _rechargeVipResult

    fun rechargeVip() {
        _rechargeVipResult.value = null
    }

    fun isVip(): Boolean {
        return accountManager.isVip()
    }

    fun getM3U8(item: VideoItem, position: Int, update: (Int, String, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val videoStreamItem =
                    item.videoEpisodes?.get(0)?.videoStreams?.get(0) ?: VideoStream()
                val result = domainManager.getApiRepository().getVideoM3u8Source(
                    videoStreamItem.id ?: 0,
                    accountManager.getProfile().userId,
                    videoStreamItem.utcTime,
                    videoStreamItem.sign
                )
                if (!result.isSuccessful) throw HttpException(result)
                val url = result.body()?.content?.streamUrl ?: ""
                emit(url)
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    e.printStackTrace()
                    val errorCode = if (e is HttpException) e.code() else -1
                    update(position, "", errorCode)
                }
                .collect {
                    getDecryptSetting(item.source ?: "")?.takeIf { it.isVideoDecrypt }
                        ?.also { decryptItem ->
                            decryptM3U8(it, decryptItem) { update(position, it, -1) }
                        } ?: run {
                        update(position, it, -1)
                    }
                }
        }
    }

    fun getInteractiveHistory(
        item: VideoItem,
        position: Int,
        update: (Int, InteractiveHistoryItem) -> Unit
    ) {
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().getInteractiveHistory(item.id.toString())
                if (!result.isSuccessful) throw HttpException(result)
                emit(result.body()?.content?.get(0))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> e.printStackTrace() }
                .collect { it?.run { update(position, this) } }
        }
    }

    fun likePost(item: VideoItem, position: Int, isLike: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id ?: 0, request)
                if (!result.isSuccessful) throw HttpException(result)

                item.like = isLike
                item.likeCount = item.likeCount?.let { if (isLike) it + 1 else it - 1 }

                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    /**
     * 影片回報問題(用於內部播放錯誤主動回報)
     */
    fun sendVideoReport(id: Long, unhealthy: Boolean) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMemberVideoReport(
                    videoId = id, type = VideoType.SHORT_VIDEO.value, unhealthy
                )
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect()
        }
    }

    /**
     * 影片回報問題(用於點擊更多)
     */
    fun sendVideoReport(id: Long, content: String) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val resp = apiRepository.sendVideoReport(ReportRequest(content, id))
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videoReport.value = it }
        }
    }

    /**
     * 加入收藏與解除收藏
     */
    fun modifyFavorite(item: VideoItem, isFavorite: Boolean, update: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val resp = when {
                    isFavorite -> apiRepository.postMePlaylist(PlayListRequest(item.id, 1))
                    else -> apiRepository.deleteMePlaylist(item.id.toString())
                }
                if (!resp.isSuccessful) throw HttpException(resp)
                val body = resp.body()?.content
                val countItem = when {
                    isFavorite -> body
                    else -> (body as ArrayList<*>)[0]
                }
                countItem as InteractiveHistoryItem
                item.favorite = isFavorite
                countItem.favoriteCount?.run { item.favoriteCount = this.toInt() }
                LruCacheUtils.putShortVideoDataCache(
                    item.id,
                    PlayItem(
                        favorite = isFavorite,
                        favoriteCount = item.favoriteCount,
                        commentCount = item.commentCount
                    )
                )
                changeFavoriteSmallVideoInDb(item.id, item.favorite, item.favoriteCount)
                emit(ApiResult.success(countItem.favoriteCount?.toInt()))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    if (it is ApiResult.Success) {
                        update(isFavorite, it.result)
                    }
                    _favoriteResult.value = it
                }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun getClips(
        orderByType: StatisticsOrderType
    ) = clearResult(orderByType)
        .flatMapConcat { clips(orderByType) }.cachedIn(viewModelScope)

    @OptIn(ExperimentalPagingApi::class)
    private fun clips(orderByType: StatisticsOrderType): Flow<PagingData<VideoItem>> {
        val pageCode = "${ClipMediator::class.java.simpleName}${orderByType.name}"
        return Pager(
            config = PagingConfig(
                pageSize = ApiRepository.NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            remoteMediator = ClipMediator(mimiDB, domainManager, orderByType, pageCode)
        ) { mimiDB.postDBItemDao().pagingSourceByPageCode(pageCode) }.flow.map { pagingData ->
            pagingData.map {
                it.memberPostItem.toVideoItem()
            }
        }
    }

    private fun clearResult(orderByType: StatisticsOrderType): Flow<Nothing?> {
        val pageCode = "${ClipMediator::class.java.simpleName}${orderByType.name}"
        return flow {
            mimiDB.postDBItemDao().deleteItemByPageCode(pageCode)
            mimiDB.remoteKeyDao().deleteByPageCode(pageCode)
            emit(null)
        }
    }

    fun getLimitClips(items: ArrayList<VideoItem>): Flow<PagingData<VideoItem>> {
        return Pager(
            config = PagingConfig(pageSize = ApiRepository.NETWORK_PAGE_SIZE),
            pagingSourceFactory = { ClipLimitPagingSource(items) }
        ).flow.cachedIn(viewModelScope)
    }

    fun resetLiveData() {
        _followResult.value = null
        _favoriteResult.value = null
        _likePostResult.value = null
        _videoReport.value = null
    }

}
