package com.dabenxiang.mimi.view.favroite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_NORMAL
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_SHORT_VIDEO
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.favroite.FavoriteFragment.Companion.TYPE_ADULT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class FavoriteViewModel : BaseViewModel() {

    val dataCount = MutableLiveData<Int>()

    private val _playList = MutableLiveData<PagedList<Any>>()
    val playList: LiveData<PagedList<Any>> = _playList

    private val _postList = MutableLiveData<PagedList<Any>>()
    val postList: LiveData<PagedList<Any>> = _postList

    fun initData(primaryType: Int, secondaryType: Int) {
        viewModelScope.launch {
            when {
                primaryType == TYPE_NORMAL || (primaryType == TYPE_ADULT && secondaryType != TYPE_SHORT_VIDEO) -> {
                    val dataSrc = FavoritePlayListDataSource(viewModelScope, domainManager, favoritePagingCallback, 1, primaryType == TYPE_ADULT)
                    dataSrc.isInvalid
                    val factory = FavoritePlayListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(FavoritePlayListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow().collect { _playList.postValue(it) }
                }

                else -> {
                    val dataSrc = FavoritePostListDataSource(viewModelScope, domainManager, favoritePagingCallback)
                    dataSrc.isInvalid
                    val factory = FavoritePostListFactory(dataSrc)
                    val config = PagedList.Config.Builder()
                        .setPageSize(FavoritePostListDataSource.PER_LIMIT.toInt())
                        .build()

                    LivePagedListBuilder(factory, config).build().asFlow().collect { _postList.postValue(it) }
                }
            }
        }
    }

    fun addLike(postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().addLike(postId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    fun deleteLike(postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().deleteLike(postId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    fun addFavorite(postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().addFavorite(postId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    fun deleteFavorite(postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().deleteFavorite(postId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    fun report(postId: Long) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().postReport(postId)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
        }
    }

    private val favoritePagingCallback = object : FavoritePagingCallback {
        override fun onLoading() { setShowProgress(true) }
        override fun onLoaded() { setShowProgress(false) }
        override fun onThrowable(throwable: Throwable) {}
        override fun onTotalCount(count: Int) { viewModelScope.launch { dataCount.value = count } }
    }
}