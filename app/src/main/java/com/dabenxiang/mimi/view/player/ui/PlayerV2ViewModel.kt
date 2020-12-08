package com.dabenxiang.mimi.view.player.ui

import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.extension.downloadFile
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.DownloadResult
import com.dabenxiang.mimi.model.api.vo.VideoEpisodeItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.api.vo.VideoM3u8Source
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class PlayerV2ViewModel: BaseViewModel() {

    private val VIDEO_CUCUMBER: String = "cucumber"

    private val _videoContentSource = MutableLiveData<ApiResult<VideoItem>>()
    val videoContentSource: LiveData<ApiResult<VideoItem>> = _videoContentSource

    private val _episodeContentSource = MutableLiveData<ApiResult<VideoEpisodeItem>>()
    val episodeContentSource: LiveData<ApiResult<VideoEpisodeItem>> = _episodeContentSource

    private val _m3u8ContentSource = MutableLiveData<ApiResult<VideoM3u8Source>>()
    val m3u8ContentSource: LiveData<ApiResult<VideoM3u8Source>> = _m3u8ContentSource

    private val _videoStreamingUrl = MutableLiveData<String>()
    val videoStreamingUrl: LiveData<String> = _videoStreamingUrl

    private val _selectSourcesPosition = MutableLiveData<Int>().also { it.value = 0 }
    val selectSourcesPosition: LiveData<Int> = _selectSourcesPosition

    private val _selectEpisodePosition = MutableLiveData<Int>()
    val selectEpisodePosition: LiveData<Int> = _selectEpisodePosition

    val showIntroduction = MutableLiveData(false)

    val stopVideoPlayer = MutableLiveData(false)

    val sourceNotFound = MutableLiveData("")

    var videoContentId : Long = -1
    var m3u8SourceUrl: String = ""
    var isResetPlayer: Boolean = true

    inner class NotDeductedException : Exception()

    /**
     * get video content info
     */
    fun getVideoContent() {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getVideoInfo(videoContentId)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.d(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect{
                    _videoContentSource.value = it
                }
        }
    }

    /**
     * parsing the video content
     */
    fun parsingVideoContent(videoItem: VideoItem) {
        viewModelScope.launch {
            flow {
                val isDeducted = videoItem.deducted!!
                if(!isDeducted) {
                    throw NotDeductedException()
                }
                val videoSource = videoItem.sources?.get(selectSourcesPosition.value!!)
                val videoEpisode = videoSource?.videoEpisodes?.get(0)
                val episodeResp = domainManager.getApiRepository().getVideoEpisode(videoContentId, videoEpisode?.id!!)
                if (!episodeResp.isSuccessful) throw HttpException(episodeResp)

                emit(ApiResult.success(episodeResp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _episodeContentSource.value = it
                }
        }
    }

    /**
     * parsing the episode content
     */
    fun parsingEpisodeContent(videoEpisodeItem: VideoEpisodeItem) {
        viewModelScope.launch {
            flow {
                val videoStreamSourceId = videoEpisodeItem.videoStreams?.get(selectEpisodePosition.value ?: 0)!!
                val streamResp = domainManager.getApiRepository().getVideoM3u8Source(
                    videoStreamSourceId.id!!,
                    accountManager.getProfile().userId,
                    videoStreamSourceId.utcTime,
                    videoStreamSourceId.sign
                )
                if (!streamResp.isSuccessful) throw HttpException(streamResp)

                emit(ApiResult.success(streamResp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect{
                    _m3u8ContentSource.value = it
                }
        }
    }

    /**
     * parsing the m3u8 content
     */
    fun parsingM3u8Content(videoM3u8Source: VideoM3u8Source) {
        viewModelScope.launch {
            flow {
                if (TextUtils.isEmpty(videoM3u8Source.streamUrl))
                    sendCrashReport(
                        "stream url is Empty, Video id ${videoM3u8Source.id}, ".plus(
                            Gson().toJson(videoM3u8Source)
                        )
                    )
                when (videoM3u8Source.isContent) {
                    false -> emit(videoM3u8Source.streamUrl)
                    true -> {
                        videoM3u8Source.streamUrl?.let { downloadM3U8(it) }
                        emit(null)
                    }
                }
            }
                .flowOn(Dispatchers.IO)
                .collect{
                    if(it != null) _videoStreamingUrl.value = it.toString()
                }
        }
    }

    private suspend fun downloadM3U8(uriString: String) {
        (HttpClient(Android) downloadFile uriString)
            .collect {
                withContext(Dispatchers.IO) {
                    when (it) {
                        is DownloadResult.Success -> {
                            if (Uri.parse((it.data as String)).isHierarchical) {
                                Timber.d("download success file path ${it.data}")
                                if(it.data.isEmpty()) sourceNotFound.postValue("")
                                else _videoStreamingUrl.postValue(it.data)
                            }
                        }
                        is DownloadResult.Error -> {
                            Timber.d("error ${it.cause}")
                            sourceNotFound.postValue("")
                        }
                        is DownloadResult.Progress -> {
                            Timber.d("progress ${it.progress}")
                        }
                        is DownloadResult.Redirect -> {
                            downloadM3U8(it.url)
                        }
                    }
                }
            }
    }

    fun selectSourcesIndex(position: Int) {
        if (position != _selectSourcesPosition.value) {
            _selectSourcesPosition.postValue(position)
        }
    }

    fun selectStreamSourceIndex(position: Int) {
        if (position != _selectEpisodePosition.value) {
            _selectEpisodePosition.postValue(position)
        }
    }

    fun clearLiveData() {
        _videoContentSource.value = null
        _episodeContentSource.value = null
        _m3u8ContentSource.value = null
        _selectSourcesPosition.value = 0
        _selectEpisodePosition.value = 0
        _videoStreamingUrl.value = null
        isResetPlayer = true
    }
}