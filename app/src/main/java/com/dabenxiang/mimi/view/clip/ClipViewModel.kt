package com.dabenxiang.mimi.view.clip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.api.vo.VideoStream
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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

    fun getM3U8(item: VideoItem, position: Int, update: (Int, String, Int) -> Unit) {
        viewModelScope.launch {
            flow {
                val videoStreamItem = item.videoEpisodes?.get(0)?.videoStreams?.get(0)?: VideoStream()
                val result = domainManager.getApiRepository().getVideoM3u8Source(videoStreamItem.id?:0, accountManager.getProfile().userId, videoStreamItem.utcTime, videoStreamItem.sign)
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
                    getDecryptSetting(item.source?:"")?.takeIf { it.isVideoDecrypt }?.also { decryptItem ->
                        decryptM3U8(it, decryptItem) { update(position, it, -1) }
                    } ?: run {
                        update(position, it, -1)
                    }
                }
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

    fun sendVideoReport(id: String, error: String) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMemberVideoReport(
                    videoId = id.toLong(), type = PostType.VIDEO.value
                )
                if (!result.isSuccessful) throw HttpException(result)
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
                .collect { _favoriteResult.value = it }
        }
    }

    fun getClips(orderByType: StatisticsOrderType): Flow<PagingData<VideoItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = ApiRepository.NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { ClipPagingSource(domainManager, orderByType) }
        ).flow.cachedIn(viewModelScope)
    }

    fun resetLiveData() {
        _followResult.value = null
        _favoriteResult.value = null
        _likePostResult.value = null
        _videoReport.value = null
    }

}
