package com.dabenxiang.mimi.view.club.latest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.MyFollowPagingCallback
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ClubLatestViewModel : BaseViewModel() {

    private val _adResult = MutableLiveData<ApiResult<AdItem>>()
    val adResult: LiveData<ApiResult<AdItem>> = _adResult

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    private val _postItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val postItemListResult: LiveData<PagedList<MemberPostItem>> = _postItemListResult

    private val _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private val _clubIdList = ArrayList<Long>()

    var totalCount: Int = 0

    fun getPostItemList() {
        viewModelScope.launch {
            getRecommendPostPagingItems()
                    .asFlow()
                    .collect { _postItemListResult.value = it }
        }
    }

    private fun getRecommendPostPagingItems(): LiveData<PagedList<MemberPostItem>> {
        val dataSourceFactory = object : DataSource.Factory<Int, MemberPostItem>() {
            override fun create(): DataSource<Int, MemberPostItem> {
                return ClubLatestListDataSource(
                        domainManager,
                        pagingCallback,
                        viewModelScope,
                        adWidth,
                        adHeight
                )
            }
        }

        val config = PagedList.Config.Builder()
                .setPrefetchDistance(4)
                .build()
        return LivePagedListBuilder(dataSourceFactory, config).build()
    }

    fun getAd() {
        viewModelScope.launch {
            flow {
                val adResult = domainManager.getAdRepository().getAD(adWidth, adHeight)
                if (!adResult.isSuccessful) throw HttpException(adResult)
                emit(ApiResult.success(adResult.body()?.content))
            }
                    .flowOn(Dispatchers.IO)
                    .collect { _adResult.value = it}
        }

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
            _clubCount.postValue(0)
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
            _clubCount.postValue(count.toInt())
        }
    }

    fun followMember(
            item: MemberPostItem,
            items: ArrayList<MemberPostItem>,
            isFollow: Boolean,
            update: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                items.forEach {
                    if (it.creatorId == item.creatorId) {
                        it.isFollow = isFollow
                    }
                }
                emit(ApiResult.success(null))
            }
                    .flowOn(Dispatchers.IO)
                    .onStart { emit(ApiResult.loading()) }
                    .onCompletion { emit(ApiResult.loaded()) }
                    .catch { e -> emit(ApiResult.error(e)) }
                    .collect { _followResult.value = it }
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
                    if (items[position].creatorId == item.creatorId)
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