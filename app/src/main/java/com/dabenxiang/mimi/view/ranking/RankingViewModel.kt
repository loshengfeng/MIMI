package com.dabenxiang.mimi.view.ranking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RankingViewModel : BaseViewModel() {

    private val _rankingList = MutableLiveData<PagedList<PostStatisticsItem>>()
    val rankingList: LiveData<PagedList<PostStatisticsItem>> = _rankingList

    private val _rankingVideosList = MutableLiveData<PagedList<StatisticsItem>>()
    val rankingVideosList: LiveData<PagedList<StatisticsItem>> = _rankingVideosList

    private var _bitmapResult = MutableLiveData<ApiResult<Int>>()
    val bitmapResult: LiveData<ApiResult<Int>> = _bitmapResult

    var statisticsTypeSelected: StatisticsType = StatisticsType.TODAY
    var postTypeSelected: PostType = PostType.VIDEO_ON_DEMAND

    fun setPostType(position: Int) {
        postTypeSelected = when (position) {
            1 -> PostType.VIDEO
            2 -> PostType.IMAGE
            else -> PostType.VIDEO_ON_DEMAND
        }
        getRankingList()
    }

    fun setStatisticsTypeFunction(position: Int) {
        statisticsTypeSelected = when (position) {
            1 -> StatisticsType.TODAY
            2 -> StatisticsType.WEEK
            else -> StatisticsType.MONTH
        }
        getRankingList()
    }

    fun getRankingList() {
        viewModelScope.launch {
            if (postTypeSelected == PostType.VIDEO_ON_DEMAND) {
                getVideosRanking()
            } else {
                getRanking()
            }
        }
    }

    private suspend fun getRanking() {
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

    private suspend fun getVideosRanking() {
        val dataSrc = RankingVideosDataSource(
            viewModelScope,
            domainManager,
            pagingCallback,
            statisticsTypeSelected
        )
        dataSrc.isInvalid
        val factory = RankingVideosFactory(dataSrc)
        val config = PagedList.Config.Builder()
            .setPageSize(RankingDataSource.PER_LIMIT.toInt())
            .build()

        LivePagedListBuilder(factory, config).build().asFlow().collect {
            _rankingVideosList.postValue(it)
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

    fun getBitmap(id: String, position: Int) {
        if (id == "0") return
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                LruCacheUtils.putLruCache(id, bitmap)
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _bitmapResult.value = it }
        }
    }

}