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

    var viewStatus: MutableMap<Int, Int> = mutableMapOf()

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

    // todo: {"code":404000,"message":"can not find post : xxxxxxxx"}
    fun modifyLike(view: TextView, postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository()
                    .addLike(postId,
                        LikeRequest(viewStatus[view.id])
                    )
                if (!result.isSuccessful) {
                    viewStatus[view.id] =
                        when (viewStatus[view.id]) {
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

    // todo: {"code":404000,"message":"The specified resource does not exist."}
    fun modifyFavorite(view: TextView, postId: Long) {
        Timber.d("addFavorite: $postId")
        viewModelScope.launch {
            flow {
                val result = if (viewStatus[view.id] == LikeType.DISLIKE.value) {
                    domainManager.getApiRepository().addFavorite(postId)
                } else {
                    domainManager.getApiRepository().deleteFavorite(postId)
                }

                if (!result.isSuccessful) {
                    viewStatus[view.id] =
                        when (viewStatus[view.id]) {
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
                .collect { _favoriteResult.value = it }
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

    fun deleteFavorite(postFavoriteId: Long, ostFavoriteIds: List<Long>) {
        viewModelScope.launch {
            flow {
                // todo: 清除此頁顯示的視頻...
                val result = domainManager.getApiRepository().deletePostFavorite(123, listOf(123))
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
    }

}