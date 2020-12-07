package com.dabenxiang.mimi.view.mypost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MyPostViewModel : BaseViewModel() {

    private val _myPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val myPostItemListResult: LiveData<PagedList<MemberPostItem>> = _myPostItemListResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private var _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private var _isNoData = MutableLiveData<Boolean>().also { it.value = false }
    val isNoData: LiveData<Boolean> = _isNoData

    var totalCount: Int = 0

    companion object {
        const val TYPE_PIC = "type_pic"
        const val TYPE_COVER = "type_cover"
        const val TYPE_VIDEO = "type_video"
        const val USER_ID_ME: Long = -1
    }

    fun checkPostEmptyUi(removeListSize: Int) {
        _isNoData.value = totalCount == removeListSize
    }

    fun getMyPost(userId: Long, isAdult: Boolean) {
        viewModelScope.launch {
            getMyPostPagingItems(userId, isAdult).asFlow()
                .collect { _myPostItemListResult.value = it }
        }
    }

    private fun getMyPostPagingItems(
        userId: Long,
        isAdult: Boolean
    ): LiveData<PagedList<MemberPostItem>> {
        val dataSourceFactory = object : DataSource.Factory<Int, MemberPostItem>() {
            override fun create(): DataSource<Int, MemberPostItem> {
                return MyPostDataSource(
                    userId,
                    isAdult,
                    pagingCallback,
                    viewModelScope,
                    domainManager
                )
            }
        }

        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(dataSourceFactory, config).build()
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
            Timber.e(throwable)
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
            totalCount = if (isInitial) count.toInt()
            else totalCount.plus(count.toInt())
            if (isInitial) cleanRemovedPosList()

            if (totalCount == 0) {
                _isNoData.value = true
            }
        }
    }

    fun likePost(item: MemberPostItem, position: Int, isLike: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val likeType = when {
                    isLike -> LikeType.LIKE
                    else -> LikeType.DISLIKE
                }
                val request = LikeRequest(likeType)
                val result = apiRepository.like(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun favoritePost(
        item: MemberPostItem,
        position: Int,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _favoriteResult.value = it }
        }
    }

    fun followPost(items: ArrayList<MemberPostItem>, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(items[position].creatorId)
                    else -> apiRepository.cancelFollowPost(items[position].creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach { item ->
                    item.isFollow = isFollow
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }
}