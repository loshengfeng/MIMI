package com.dabenxiang.mimi.view.player

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.Source
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.VideoConsumeResult
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.HttpException
import timber.log.Timber

class PlayerViewModel : BaseViewModel() {

    private val apiRepository: ApiRepository by inject()

    companion object {
        var volume: Float = 1f
    }

    var videoId: Long = 0L

    var currentWindow: Int = 0
    var playbackPosition: Long = 0
    var lockFullScreen = false
    var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private val _fastForwardTime = MutableLiveData<Int>()
    val fastForwardTime: LiveData<Int> = _fastForwardTime

    private val _soundLevel = MutableLiveData<Float>()
    val soundLevel: LiveData<Float> = _soundLevel

    private val _isLoadingActive = MutableLiveData<Boolean>()
    val isLoadingActive: LiveData<Boolean> = _isLoadingActive

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentVideoUrl = MutableLiveData<String?>()
    val currentVideoUrl: LiveData<String?> = _currentVideoUrl

    private val _apiVideoInfo = MutableLiveData<ApiResult<VideoItem>>()
    val apiVideoInfo: LiveData<ApiResult<VideoItem>> = _apiVideoInfo

    private val _consumeResult = MutableLiveData<VideoConsumeResult>()
    val consumeResult: LiveData<VideoConsumeResult> = _consumeResult

    private val _sourceListPosition = MutableLiveData<Int>()
    val sourceListPosition: LiveData<Int> = _sourceListPosition

    private val _streamPosition = MutableLiveData<Int>()
    val streamPosition: LiveData<Int> = _streamPosition

    val showIntroduction = MutableLiveData(false)

    var sourceList: List<Source>? = null
    val likeVideo = MutableLiveData<Boolean>()
    val favoriteVideo = MutableLiveData<Boolean>()
    val likeVideoCount = MutableLiveData<Long>()
    val favoriteVideoCount = MutableLiveData<Long>()
    val commentCount = MutableLiveData<Long>()

    var isDeducted = false
    var costPoint = 0L
    var availablePoint = 0L

    fun setFastForwardTime(time: Int) {
        _fastForwardTime.value = time
    }

    fun setRewindTime(time: Int) {
        _fastForwardTime.value = -time
    }

    fun setSoundLevel(level: Float) {
        _soundLevel.value = if (level > 1) 1f else if (level < 0) 0f else level
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun activateLoading(isLoading: Boolean) {
        viewModelScope.launch {
            flow {
                if (isLoading)
                    delay(2000)

                emit(isLoading)
            }
                .flowOn(Dispatchers.IO)
                .catch { emit(true) }
                .collect { _isLoadingActive.value = it }
        }
    }

    fun getMediaSource(uriString: String, sourceFactory: DefaultDataSourceFactory): MediaSource? {
        val uri = Uri.parse(uriString)

        val sourceType = Util.inferContentType(uri)
        //Timber.d("#sourceType: $sourceType")

        return when (sourceType) {
            C.TYPE_DASH ->
                DashMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_HLS ->
                HlsMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_SS ->
                SsMediaSource.Factory(sourceFactory)
                    .createMediaSource(uri)
            C.TYPE_OTHER -> {
                when {
                    uriString.startsWith("rtmp://") ->
                        ProgressiveMediaSource.Factory(RtmpDataSourceFactory())
                            .createMediaSource(uri)
                    uriString.contains("m3u8") -> HlsMediaSource.Factory(sourceFactory)
                        .createMediaSource(uri)
                    else ->
                        ProgressiveMediaSource.Factory(sourceFactory)
                            .createMediaSource(uri)
                }
            }
            else -> null
        }
    }

    fun getVideoInfo() {
        viewModelScope.launch {
            flow {
                val resp = apiRepository.getVideoInfo(videoId)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.e(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiVideoInfo.value = it
                }
        }
    }

    fun checkConsumeResult() {
        val result =
            when {
                costPoint == 0L || isDeducted -> VideoConsumeResult.Paid
                else -> when {
                    availablePoint > costPoint -> VideoConsumeResult.PaidYet
                    else -> VideoConsumeResult.PointNotEnough
                }
            }

        _consumeResult.value = result
    }

    fun setSourceListPosition(position: Int) {
        if (position != _sourceListPosition.value) {
            _sourceListPosition.value = position
        }
    }

    fun setStreamPosition(position: Int) {
        if (position != _streamPosition.value) {
            _streamPosition.value = position
        }
    }
}