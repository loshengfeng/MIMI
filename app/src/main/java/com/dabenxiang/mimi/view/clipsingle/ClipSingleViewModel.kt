package com.dabenxiang.mimi.view.clipsingle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClipSingleViewModel: BaseViewModel() {

    private var _favoriteResult = MutableLiveData<ApiResult<Nothing>>()
    val favoriteResult: LiveData<ApiResult<Nothing>> = _favoriteResult

    private val _videoReport = MutableLiveData<ApiResult<Nothing>>()
    val videoReport: LiveData<ApiResult<Nothing>> = _videoReport

    private var _getM3U8Result = MutableLiveData<ApiResult<String>>()
    val getM3U8Result: LiveData<ApiResult<String>> = _getM3U8Result


    /**
     * 加入收藏與解除收藏
     */
    fun modifyFavorite(item: PlayItem, isFavorite: Boolean) {
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
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun sendVideoReport(id: String) {
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


    var videoItem: VideoItem? = null
    var videoEpisodeItem: VideoEpisodeItem? = null
    var videoM3u8Source: VideoM3u8Source? = null

    /**
     * parsing the episode content
     */
    fun getM3U8(playItem: PlayItem) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val videoInfoResp = apiRepository.getVideoInfo(playItem.videoId?:0)
                if (!videoInfoResp.isSuccessful) throw HttpException(videoInfoResp)
                videoItem = videoInfoResp.body()?.content
                val videoEpisode = videoItem?.sources?.get(0)?.videoEpisodes?.get(0)

                val episodeResp = apiRepository.getVideoEpisode(playItem.videoId?:0, videoEpisode?.id?:0)
                if (!episodeResp.isSuccessful) throw HttpException(episodeResp)
                videoEpisodeItem = episodeResp.body()?.content

                val videoStream = videoEpisodeItem?.videoStreams?.get(0)
                val streamResp = domainManager.getApiRepository().getVideoM3u8Source(
                    videoStream?.id?:0,
                    accountManager.getProfile().userId,
                    videoStream?.utcTime?:0,
                    videoStream?.sign?:""
                )
                if (!streamResp.isSuccessful) throw HttpException(streamResp)
                videoM3u8Source = streamResp.body()?.content
                emit(ApiResult.success(videoM3u8Source?.streamUrl?:""))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect{
                    _getM3U8Result.value = it
                }
        }
    }

    fun resetLiveData() {
        _favoriteResult.value = null
        _videoReport.value = null
    }

}