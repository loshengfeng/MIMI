package com.dabenxiang.mimi.view.favroite

import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.callback.FavoritePagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.PlayListRequest
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_ADULT
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_NORMAL
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_SHORT_VIDEO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class FavoriteViewModel : BaseViewModel() {

    val dataCount = MutableLiveData<Int>()

    val videoIDList = ArrayList<Long>()

    var viewStatus: MutableMap<Long, Int> = mutableMapOf()
    var viewFavoriteStatus: MutableMap<Long, Int> = mutableMapOf()

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _playList = MutableLiveData<PagedList<Any>>()
    val playList: LiveData<PagedList<Any>> = _playList

    private val _postList = MutableLiveData<PagedList<Any>>()
    val postList: LiveData<PagedList<Any>> = _postList

    private val _likeResult = MutableLiveData<ApiResult<TextView>>()
    val likeResult: LiveData<ApiResult<TextView>> = _likeResult

    private val _favoriteResult = MutableLiveData<ApiResult<TextView>>()
    val favoriteResult: LiveData<ApiResult<TextView>> = _favoriteResult

    private val _reportResult = MutableLiveData<ApiResult<TextView>>()
    val reportResult: LiveData<ApiResult<TextView>> = _reportResult

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

    fun getAttachment(view: ImageView, id: Long) {
        if (!setImage(view, id)) {
            viewModelScope.launch {
                flow {
                    val result = domainManager.getApiRepository().getAttachment(id)
                    if (!result.isSuccessful) throw HttpException(result)

                    val byteArray = result.body()?.bytes()
                    val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                    if (bitmap != null) {
                        lruCacheManager.putLruCache(id, bitmap)
                        setImage(view, id)
                    }
                    emit(ApiResult.success(Pair(view, id)))
                }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { resp ->
                        when (resp) {
                            is ApiResult.Error -> Timber.e(resp.throwable)
                            is ApiResult.Success -> {
                            }
                        }
                    }
            }
        }
    }

    private fun setImage(view: ImageView, id: Long): Boolean {
        val bitmap = lruCacheManager.getLruCache(id)

        return when (lruCacheManager.getLruCache(id)) {
            null -> false
            else -> {
                val options: RequestOptions = RequestOptions()
                    .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .priority(Priority.NORMAL)

                Glide.with(App.self).load(bitmap)
                    .apply(options)
                    .into(view)
                true
            }
        }
    }

    fun modifyLike(view: TextView, videoID: Long) {
        view.tag = videoID
        viewStatus[videoID] = when (viewStatus[videoID]) {
            LikeType.LIKE.value -> LikeType.DISLIKE.value
            LikeType.DISLIKE.value -> LikeType.LIKE.value
            else -> LikeType.LIKE.value
        }

        val likeRequest = LikeRequest(viewStatus[videoID])
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .addLike(videoID, likeRequest)
                if (!result.isSuccessful) {
                    viewStatus[videoID] =
                            when (viewStatus[videoID]) {
                                LikeType.LIKE.value -> LikeType.DISLIKE.value
                                LikeType.DISLIKE.value -> LikeType.LIKE.value
                                else -> LikeType.LIKE.value
                            }
                    throw HttpException(result)
                }
                emit(ApiResult.success(view))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _likeResult.value = it }
        }
    }

    fun modifyFavorite(view: TextView, videoID: Long) {
        view.tag = videoID
        viewModelScope.launch {
            flow {
                val result = if (viewFavoriteStatus[videoID] == LikeType.DISLIKE.value) {
                    domainManager.getApiRepository().postMePlaylist(PlayListRequest(videoID, 1))
                } else {
                    domainManager.getApiRepository().deleteMePlaylist(videoID.toString())
                }

                if (!result.isSuccessful) {
                    throw HttpException(result)
                }

                viewFavoriteStatus[videoID] =
                        when (viewFavoriteStatus[videoID]) {
                            LikeType.LIKE.value -> LikeType.DISLIKE.value
                            LikeType.DISLIKE.value -> LikeType.LIKE.value
                            else -> LikeType.LIKE.value
                        }
                emit(ApiResult.success(view))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _favoriteResult.value = it }
        }
    }

    fun report(postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().postReport(postId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    fun deleteFavorite() {
        if (videoIDList.size == 0) return
        viewModelScope.launch {
            flow {
                // todo: 清除此頁顯示的視頻...
                val result = domainManager.getApiRepository().deleteMePlaylist(videoIDList.joinToString(separator = ","))
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

        override fun onTotalVideoId(ids: ArrayList<Long>) {
            videoIDList.clear()
            videoIDList.addAll(ids)
        }
    }

}