package com.dabenxiang.mimi.view.favroite

import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.FavoritePagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_ADULT
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_NORMAL
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_SHORT_VIDEO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class FavoriteViewModel : BaseViewModel() {

    val dataCount = MutableLiveData<Int>()

    val videoIDList = ArrayList<Long>()

    var currentPlayItem: PlayItem? = null
    var currentPostItem: MemberPostItem? = null
    var currentPostList: ArrayList<MemberPostItem> = ArrayList()

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _playList = MutableLiveData<PagedList<Any>>()
    val playList: LiveData<PagedList<Any>> = _playList

    private val _postList = MutableLiveData<PagedList<Any>>()
    val postList: LiveData<PagedList<Any>> = _postList

    private val _likeResult = MutableLiveData<ApiResult<Long>>()
    val likeResult: LiveData<ApiResult<Long>> = _likeResult

    private val _followResult = MutableLiveData<ApiResult<Long>>()
    val followResult: LiveData<ApiResult<Long>> = _followResult

    private val _favoriteResult = MutableLiveData<ApiResult<Long>>()
    val favoriteResult: LiveData<ApiResult<Long>> = _favoriteResult

    private val _reportResult = MutableLiveData<ApiResult<TextView>>()
    val reportResult: LiveData<ApiResult<TextView>> = _reportResult

    private var _attachmentByTypeResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentByTypeResult: LiveData<ApiResult<AttachmentItem>> = _attachmentByTypeResult

    private val _isEmailConfirmed = MutableLiveData<ApiResult<Boolean>>()
    val isEmailConfirmed: LiveData<ApiResult<Boolean>> get() = _isEmailConfirmed

    fun initData(primaryType: Int, secondaryType: Int) {
        viewModelScope.launch {
            when {
                primaryType == TYPE_NORMAL || (primaryType == TYPE_ADULT && secondaryType != TYPE_SHORT_VIDEO) -> {
                    val dataSrc = FavoritePlayListDataSource(
                        viewModelScope,
                        domainManager,
                        favoritePagingCallback,
                        1,
                        primaryType == TYPE_ADULT
                    )
                    dataSrc.isInvalid
                    val factory = FavoritePlayListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(FavoritePlayListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow()
                        .collect { _playList.postValue(it) }
                }

                else -> {
                    currentPostList.clear()
                    val dataSrc = FavoritePostListDataSource(
                        viewModelScope,
                        domainManager,
                        favoritePagingCallback
                    )
                    dataSrc.isInvalid
                    val factory = FavoritePostListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(FavoritePostListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow()
                        .collect { _postList.postValue(it) }
                }
            }
        }
    }

    fun modifyLike(videoID: Long) {
        val likeType = if (currentPlayItem?.like == true) LikeType.DISLIKE else LikeType.LIKE
        val likeRequest = LikeRequest(likeType)
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .like(videoID, likeRequest)
                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                emit(ApiResult.success(videoID))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    currentPlayItem?.run {
                        like = like != true
                        likeCount = if (like == true) (likeCount ?: 0) + 1 else (likeCount ?: 0) - 1
                    }
                    _likeResult.value = it
                }
        }
    }

    fun modifyFavorite(videoID: Long) {
        viewModelScope.launch {
            flow {
                val result = if (currentPlayItem?.favorite == false) {
                    domainManager.getApiRepository().postMePlaylist(PlayListRequest(videoID, 1))
                } else {
                    domainManager.getApiRepository().deleteMePlaylist(videoID.toString())
                }

                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                emit(ApiResult.success(videoID))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _favoriteResult.value = it
                }
        }
    }

    fun removePostFavorite(id: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().deleteFavorite(id)
                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                emit(ApiResult.success(id))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .collect {
                        _favoriteResult.value = it
                    }
        }
    }

    fun modifyPostLike(videoID: Long) {
        val likeType = if (currentPostItem?.likeType == LikeType.LIKE) LikeType.DISLIKE else LikeType.LIKE
        val likeRequest = LikeRequest(likeType)
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                        .like(videoID, likeRequest)
                if (!result.isSuccessful) {
                    throw HttpException(result)
                }
                emit(ApiResult.success(videoID))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .collect {
                        currentPostItem?.let {item->
                            item.likeType = if (item.likeType == LikeType.LIKE) LikeType.DISLIKE else LikeType.LIKE
                            item.likeCount = if (item.likeType == LikeType.LIKE) (item.likeCount ?: 0) + 1 else (item.likeCount ?: 0) - 1
                        }
                        _likeResult.value = it
                    }
        }
    }

    fun modifyFollow(posterId:Long , isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    !isFollow -> apiRepository.followPost(posterId)
                    else -> apiRepository.cancelFollowPost(posterId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                currentPostItem?.isFollow = !isFollow
                emit(ApiResult.success(posterId))
            }
                    .flowOn(Dispatchers.IO)
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _followResult.value = it }
        }
    }

    fun report(postId: Long, content: String) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().sendPostReport(postId, ReportRequest(content))
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    /**
     * 刪除所有該tab下除了短視頻的影片
     */
    fun deleteFavorite() {
        if (videoIDList.size == 0) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .deleteMePlaylist(videoIDList.joinToString(separator = ","))
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

    /**
     * 刪除全部的短視頻影片
     */
    fun deleteAllPostFavorite(){
        if (videoIDList.size == 0) return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                        .deletePostFavorite(videoIDList.joinToString(separator = ","))
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

    fun getAttachment(id: String, position: Int, type: AttachmentType) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                        id = id,
                        bitmap = bitmap,
                        position = position,
                        type = type
                )
                emit(ApiResult.success(item))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _attachmentByTypeResult.value = it }
        }


    }

    private val favoritePagingCallback = object : FavoritePagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}
        override fun onTotalCount(count: Int) {
            viewModelScope.launch { dataCount.value = count }
        }

        override fun onTotalVideoId(ids: ArrayList<Long>, clear: Boolean) {
            if (clear)
                videoIDList.clear()
            videoIDList.addAll(ids)
        }

        override fun onReceiveResponse(response: ArrayList<MemberPostItem>) {
            currentPostList.addAll(response)
        }
    }

    fun checkEmailConfirmed() {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMe()
                val isEmailConfirmed = result.body()?.content?.isEmailConfirmed ?: false
                val status = result.isSuccessful && isEmailConfirmed
                emit(ApiResult.success(status))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _isEmailConfirmed.value = it }
        }
    }
}