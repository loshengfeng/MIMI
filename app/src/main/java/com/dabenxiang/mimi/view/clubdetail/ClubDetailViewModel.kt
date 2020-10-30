package com.dabenxiang.mimi.view.clubdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ClubDetailViewModel : BaseViewModel() {

    var adWidth = 0
    var adHeight = 0

    private var _followClubResult = MutableLiveData<ApiResult<Boolean>>()
    val followClubResult: LiveData<ApiResult<Boolean>> = _followClubResult

    private var _updateCountHottest = MutableLiveData<Int>()
    val updateCountHottest: LiveData<Int> = _updateCountHottest

    private var _updateCountNewest = MutableLiveData<Int>()
    val updateCountNewest: LiveData<Int> = _updateCountNewest

    private var _updateCountVideo = MutableLiveData<Int>()
    val updateCountVideo: LiveData<Int> = _updateCountVideo

    fun getMemberPosts(
        tag: String,
        orderBy: OrderBy,
        update: ((PagedList<MemberPostItem>) -> Unit)
    ) {
        viewModelScope.launch {
            getMemberPostPagingItems(tag, orderBy).asFlow()
                .collect { update(it) }
        }
    }

    private fun getMemberPostPagingItems(
        tag: String,
        orderBy: OrderBy
    ): LiveData<PagedList<MemberPostItem>> {
        val pagingCallback = object : PagingCallback {
            override fun onLoading() {
                setShowProgress(true)
            }

            override fun onLoaded() {
                setShowProgress(false)
            }

            override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
                if (isInitial) cleanRemovedPosList()
                when (orderBy) {
                    OrderBy.HOTTEST -> _updateCountHottest.value = count.toInt()
                    OrderBy.NEWEST -> _updateCountNewest.value = count.toInt()
                    OrderBy.VIDEO -> _updateCountVideo.value = count.toInt()
                }
            }
        }
        val clubDetailPostDataSource =
            ClubDetailPostDataSource(
                pagingCallback,
                viewModelScope,
                domainManager,
                tag,
                orderBy,
                adWidth,
                adHeight
            )
        val clubDetailPostFactory = ClubDetailPostFactory(clubDetailPostDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(clubDetailPostFactory, config).build()
    }

    fun followMember(
        item: MemberPostItem,
        items: List<MemberPostItem>,
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
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Empty -> update(isFollow)
                    }
                }
        }
    }

    fun likePost(item: MemberPostItem, isLike: Boolean, update: (Boolean, Int) -> Unit) {
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

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }
                emit(ApiResult.success(item.likeCount))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(isLike, it.result)
                        }
                    }
                }
        }
    }

    fun followClub(item: MemberClubItem, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followClub(item.id)
                    else -> apiRepository.cancelFollowClub(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(isFollow))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followClubResult.value = it }
        }
    }
}