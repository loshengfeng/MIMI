package com.dabenxiang.mimi.view.player

import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.GuessLikePagingCallBack
import com.dabenxiang.mimi.event.SingleLiveEvent
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.VideoConsumeResult
import com.dabenxiang.mimi.model.holder.BaseVideoItem
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
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class PlayerViewModel : BaseViewModel() {

    companion object {
        var volume: Float = 1f
        const val StreamUrlFormat = "%s/v1/Player/%d/%d/%d?userId=%d&utcTime=%d&sign=%s"
    }

    var videoId: Long = 0L // 一開始用最外層的id, 點選影片之後用裡面的 source id
    var category: String = ""
    var episodeId: Long = -1L
    var isReported: Boolean = false

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

    private val _apiVideoInfo = MutableLiveData<ApiResult<VideoItem>>()
    val apiVideoInfo: LiveData<ApiResult<VideoItem>> = _apiVideoInfo

    private val _apiStreamResult = MutableLiveData<ApiResult<Nothing>>()
    val apiStreamResult: LiveData<ApiResult<Nothing>> = _apiStreamResult

    private val _apiLoadReplyCommentResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiLoadReplyCommentResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> =
        _apiLoadReplyCommentResult

    private val _apiPostCommentResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiPostCommentResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _apiPostCommentResult

    private val _apiCommentLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiCommentLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _apiCommentLikeResult

    private val _apiDeleteCommentLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiDeleteCommentLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> =
        _apiDeleteCommentLikeResult

    private val _consumeResult = MutableLiveData<VideoConsumeResult>()
    val consumeResult: LiveData<VideoConsumeResult> = _consumeResult

    private val _sourceListPosition = MutableLiveData<Int>()
    val sourceListPosition: LiveData<Int> = _sourceListPosition

    private val _episodePosition = MutableLiveData<Int>()
    val episodePosition: LiveData<Int> = _episodePosition

    private val _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private val _recyclerViewGuessLikeVisible = MutableLiveData<Int>()
    val recyclerViewGuessLikeVisible: LiveData<Int> = _recyclerViewGuessLikeVisible

    private val _isSelectedNewestComment = MutableLiveData(true)
    val isSelectedNewestComment: LiveData<Boolean> = _isSelectedNewestComment

    private val _apiAddFavoriteResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiAddFavoriteResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> =
        _apiAddFavoriteResult

    private val _apiAddLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiAddLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> =
        _apiAddLikeResult

    private val _apiReportResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiReportResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _apiReportResult

    fun updatedSelectedNewestComment(isNewest: Boolean) {
        _isSelectedNewestComment.value = isNewest
    }

    val showIntroduction = MutableLiveData(false)

    var nextVideoUrl: String? = null
    var currentVideoUrl: String? = null
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
                val resp = domainManager.getApiRepository().getVideoInfo(videoId)
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

    fun getStreamUrl(isAdult: Boolean) {
        if (isAdult) {
            getAdultStreamUrl()
        } else {
            getStreamUrl()
        }
    }

    private fun updated() {

    }

    private fun getAdultStreamUrl() {
        viewModelScope.launch {
            flow {
                val source = sourceList?.get(sourceListPosition.value!!)!!
                val episode = source.videoEpisodes?.get(0)!!
                val episodeId = episode.id!!

                val apiRepository = domainManager.getApiRepository()

                val episodeResp = apiRepository.getVideoEpisode(videoId, episodeId)
                if (!episodeResp.isSuccessful) throw HttpException(episodeResp)

                if (!isDeducted) {
                    val videoInfoResp = domainManager.getApiRepository().getVideoInfo(videoId)
                    if (!videoInfoResp.isSuccessful) throw HttpException(videoInfoResp)
                    isDeducted = videoInfoResp.body()?.content?.deducted ?: false
                }

                if (!isDeducted) throw Exception("點數不足")

                val episodeInfo = episodeResp.body()?.content
                val videoStreamsSize = episodeInfo?.videoStreams?.size ?: 0
                val selectedEpisodeIndex = episodePosition.value ?: 0

                val stream = episodeInfo?.videoStreams?.get(
                    if (videoStreamsSize > selectedEpisodeIndex)
                        selectedEpisodeIndex
                    else
                        0
                )!!

                val streamResp = apiRepository.getVideoVideoStreamM3u8(
                    stream.id!!,
                    accountManager.getProfile().userId,
                    stream.utcTime,
                    stream.sign
                )
                if (!streamResp.isSuccessful) throw HttpException(streamResp)
                // 取得轉址Url
                nextVideoUrl = streamResp.raw().request.url.toString()

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.e(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiStreamResult.value = it
                }
        }
    }

    private fun getStreamUrl() {
        viewModelScope.launch {
            flow {
                val source = sourceList?.get(sourceListPosition.value!!)!!
                val episode = source.videoEpisodes?.get(episodePosition.value!!)!!
                episodeId = episode.id ?: 0L

                val apiRepository = domainManager.getApiRepository()

                val episodeResp = apiRepository.getVideoEpisode(videoId, episodeId)
                if (!episodeResp.isSuccessful) throw HttpException(episodeResp)

                if (!isDeducted) {
                    val videoInfoResp = domainManager.getApiRepository().getVideoInfo(videoId)
                    if (!videoInfoResp.isSuccessful) throw HttpException(videoInfoResp)
                    isDeducted = videoInfoResp.body()?.content?.deducted ?: false
                }

                if (!isDeducted) throw Exception("點數不足")

                val episodeInfo = episodeResp.body()?.content
                isReported = episodeInfo?.reported ?: false
                videoId = episodeInfo?.id ?: 0

                val stream = episodeInfo?.videoStreams?.get(0)!!
                val streamResp = apiRepository.getVideoStreamOfEpisode(
                    videoId,
                    episodeId,
                    stream.id!!,
                    accountManager.getProfile().userId,
                    stream.utcTime,
                    stream.sign
                )
                if (!streamResp.isSuccessful) throw HttpException(streamResp)
                // 取得轉址Url
                nextVideoUrl = streamResp.raw().request.url.toString()

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.e(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiStreamResult.value = it
                }
        }
    }

    fun checkConsumeResult() {
        val result =
            when {
                costPoint == 0L || isDeducted -> VideoConsumeResult.PAID
                else -> when {
                    availablePoint > costPoint -> VideoConsumeResult.PAID_YET
                    else -> VideoConsumeResult.POINT_NOT_ENOUGH
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
        if (position != _episodePosition.value) {
            _episodePosition.value = position
        }
    }

    fun setupGuessLikeList(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            val dataSrc = GuessLikeDataSource(
                isAdult,
                category ?: "",
                viewModelScope,
                domainManager.getApiRepository(),
                pagingCallback
            )
            val factory = GuessLikeFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(GuessLikeDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _videoList.postValue(it)
            }
        }
    }

    private val pagingCallback = object : GuessLikePagingCallBack {
        override fun onLoadInit(initCount: Int) {
            _recyclerViewGuessLikeVisible.value =
                if (initCount == 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        }

        override fun onLoading() {
        }

        override fun onLoaded() {
        }

        override fun onThrowable(throwable: Throwable) {
        }
    }

    fun setupCommentDataSource(adapter: CommentAdapter) {
        viewModelScope.launch {
            val sorting = if (isSelectedNewestComment.value == true) 1 else 2
            val dataSrc = CommentDataSource(videoId, sorting, domainManager)

            dataSrc.loadMore().also { load ->
                withContext(Dispatchers.Main) {
                    load.content?.let { list ->
                        val finalList = list.map { item ->
                            RootCommentNode(item)
                        }
                        adapter.setList(finalList)
                    }
                }
                setupLoadMoreResult(adapter, load.isEnd)
            }

            adapter.loadMoreModule.setOnLoadMoreListener {
                viewModelScope.launch(Dispatchers.IO) {
                    dataSrc.loadMore().also { load ->
                        withContext(Dispatchers.Main) {
                            load.content?.also { list ->
                                val finalList = list.map { item ->
                                    RootCommentNode(item)
                                }
                                adapter.addData(finalList)
                            }
                            setupLoadMoreResult(adapter, load.isEnd)
                        }
                    }
                }
            }
        }
    }

    private fun setupLoadMoreResult(adapter: CommentAdapter, isEnd: Boolean) {
        if (isEnd) {
            adapter.loadMoreModule.loadMoreEnd()
        } else {
            adapter.loadMoreModule.loadMoreComplete()
        }
    }

    fun loadReplyComment(parentNode: RootCommentNode, commentId: Long) {
        viewModelScope.launch {
            flow {
                var isFirst = true
                var total = 0L
                var offset = 0L
                var currentSize = 0
                while (isFirst || replyCommentHasNextPage(total, offset, currentSize)) {
                    isFirst = false

                    currentSize = 0

                    val resp = domainManager.getApiRepository()
                        .getMembersPostComment(
                            postId = videoId,
                            parentId = commentId,
                            sorting = 1,
                            offset = "0",
                            limit = "50"
                        )
                    if (!resp.isSuccessful) throw HttpException(resp)

                    parentNode.nestedCommentList.clear()
                    resp.body()?.content?.map {
                        NestedCommentNode(parentNode, it)
                    }?.also {
                        parentNode.nestedCommentList.addAll(it)

                        val pagingItem = resp.body()?.paging
                        total = pagingItem?.count ?: 0L
                        offset = pagingItem?.offset ?: 0L
                        currentSize = it.size
                    }

                    if (currentSize == 0)
                        break
                }

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.e(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiLoadReplyCommentResult.value = SingleLiveEvent(it)
                }
        }
    }

    private fun replyCommentHasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < 50 -> false
            offset >= total -> false
            else -> true
        }
    }

    fun postComment(body: PostCommentRequest) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().postMembersPostComment(videoId, body)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiPostCommentResult.value = SingleLiveEvent(it)
                }
        }
    }

    fun postCommentLike(commentId: Long, body: PostLikeRequest) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository()
                    .postMembersPostCommentLike(videoId, commentId, body)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiCommentLikeResult.value = SingleLiveEvent(it)
                }
        }
    }

    fun deleteCommentLike(commentId: Long) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository()
                    .deleteMembersPostCommentLike(videoId, commentId)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiDeleteCommentLikeResult.value = SingleLiveEvent(it)
                }
        }
    }

    /**
     * 加入收藏與解除收藏
     */
    fun modifyFavorite() {
        viewModelScope.launch {
            flow {
                val resp = if (favoriteVideo.value == true)
                    domainManager.getApiRepository()
                        .deleteMePlaylist(videoId.toString())
                else
                    domainManager.getApiRepository()
                        .postMePlaylist(PlayListRequest(videoId, 1))

                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiAddFavoriteResult.value = SingleLiveEvent(it)
                }
        }
    }

    /**
     * 按讚、取消讚
     */
    fun modifyLike() {
        viewModelScope.launch {
            flow {
                val likeType = if (likeVideo.value == true) LikeType.DISLIKE else LikeType.LIKE
                val resp = domainManager.getApiRepository()
                    .like(videoId, LikeRequest(likeType))
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiAddLikeResult.value = SingleLiveEvent(it)
                }
        }
    }

    /**
     * 問題回報
     */
    fun sentReport(content: String) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().sendPostReport(
                    videoId, ReportRequest(content)
                )
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiReportResult.value = SingleLiveEvent(it)
                }
        }
    }
}