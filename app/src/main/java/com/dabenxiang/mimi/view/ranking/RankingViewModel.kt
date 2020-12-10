package com.dabenxiang.mimi.view.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class RankingViewModel : BaseViewModel() {

    private val _rankingList = MutableLiveData<PagedList<PostStatisticsItem>>()
    val rankingList: LiveData<PagedList<PostStatisticsItem>> = _rankingList

    private val _rankingClipList = MutableLiveData<PagedList<VideoItem>>()
    val rankingClipList: LiveData<PagedList<VideoItem>> = _rankingClipList

    private val _rankingVideosList = MutableLiveData<PagedList<StatisticsItem>>()
    val rankingVideosList: LiveData<PagedList<StatisticsItem>> = _rankingVideosList

    private val _postDetail = MutableLiveData<ApiResult<MemberPostItem>>()
    val postDetail: LiveData<ApiResult<MemberPostItem>> = _postDetail

    var statisticsTypeSelected: StatisticsType = StatisticsType.TODAY
    var postTypeSelected: PostType = PostType.VIDEO_ON_DEMAND

    fun setPostType(position: Int) {
        postTypeSelected = when (position) {
            0 -> PostType.VIDEO_ON_DEMAND
            1 -> PostType.VIDEO
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

    fun getRankingPostList() {
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

    fun getVideosRanking() {
        viewModelScope.launch {
            val timeRange = getTimeRange()
            val dataSrc = RankingVideosDataSource(
                viewModelScope,
                domainManager,
                pagingCallback,
                timeRange.first,
                timeRange.second
            )
            dataSrc.isInvalid
            val factory = RankingVideosFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(RankingVideosDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _rankingVideosList.postValue(it)
            }
        }
    }

    fun getRankingClipList() {
        viewModelScope.launch {
            val timeRange = getTimeRange()
            val dataSrc = RankingClipDataSource(
                viewModelScope,
                domainManager,
                pagingCallback,
                timeRange.first,
                timeRange.second
            )
            dataSrc.isInvalid
            val factory = RankingClipFactory(dataSrc)
            val config = PagedList.Config.Builder()
                .setPageSize(RankingClipDataSource.PER_LIMIT.toInt())
                .build()

            LivePagedListBuilder(factory, config).build().asFlow().collect {
                _rankingClipList.postValue(it)
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

    private fun getTimeRange(): Pair<String, String> {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        when (statisticsTypeSelected) {
            StatisticsType.TODAY -> {
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)

                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
            }
            StatisticsType.WEEK -> {
                start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek + 1)
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)

                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
            }
            StatisticsType.MONTH -> {
                start.set(Calendar.DAY_OF_MONTH, start.getActualMinimum(Calendar.DAY_OF_MONTH))
                start.set(Calendar.HOUR_OF_DAY, 0)
                start.set(Calendar.MINUTE, 0)
                start.set(Calendar.SECOND, 0)

                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
            }
        }
        return Pair(GeneralUtils.parseTimeToUTC(start.time), GeneralUtils.parseTimeToUTC(end.time))
    }
}