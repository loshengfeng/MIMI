package com.dabenxiang.mimi.view.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class RankingViewModel : BaseViewModel() {

    private val _rankingList = MutableLiveData<PagedList<PostStatisticsItem>>()
    val rankingList: LiveData<PagedList<PostStatisticsItem>> = _rankingList

    private val _postDetail = MutableLiveData<ApiResult<MemberPostItem>>()
    val postDetail: LiveData<ApiResult<MemberPostItem>> = _postDetail

    private val _clipInteractiveHistory = MutableLiveData<ApiResult<VideoItem>>()
    val clipInteractiveHistory: LiveData<ApiResult<VideoItem>> = _clipInteractiveHistory

    var statisticsTypeSelected: StatisticsType = StatisticsType.TODAY
    var postTypeSelected: PostType = PostType.VIDEO_ON_DEMAND

    fun setPostType(position: Int) {
        postTypeSelected = when (position) {
            0 -> PostType.VIDEO_ON_DEMAND
            1 -> PostType.SMALL_CLIP
            else -> PostType.IMAGE
        }
    }

    fun setStatisticsTypeFunction(position: Int) {
        statisticsTypeSelected = when (position) {
            0 -> StatisticsType.TODAY
            1 -> StatisticsType.WEEK
            else -> StatisticsType.MONTH
        }
    }

    fun getRankingList() {
        viewModelScope.launch {
            val dataSrc = RankingDataSource(
                viewModelScope,
                domainManager,
                pagingCallback,
                statisticsTypeSelected,
                postTypeSelected
            )
            dataSrc.isInvalid
            val factory = RankingFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(RankingDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _rankingList.postValue(it)
            }
        }
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {}
    }

    fun getPostDetail(id: Long) {
        viewModelScope.launch {
            flow {
                val resultPost = domainManager.getApiRepository().getMemberPostDetail(id)
                if (!resultPost.isSuccessful) throw HttpException(resultPost)
                emit(ApiResult.success(resultPost.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.d(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _postDetail.value = it
                }
        }
    }

    fun getInteractiveHistory(item: PostStatisticsItem) {
        viewModelScope.launch {
            flow {
                val result =
                    domainManager.getApiRepository().getInteractiveHistory(item.id.toString())
                if (!result.isSuccessful) throw HttpException(result)
                val countItem = result.body()?.content?.get(0)
                emit(
                    ApiResult.success(
                        VideoItem(
                            id = item.id,
                            title = item.title,
                            cover = item.cover,
                            source = item.source,
                            favorite = countItem?.isFavorite ?: false,
                            favoriteCount = countItem?.favoriteCount?.toInt() ?: 0,
                            commentCount = countItem?.commentCount?.toInt() ?: 0
                        )
                    )
                )
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    _clipInteractiveHistory.value = it
                }
        }
    }

}