package com.dabenxiang.mimi.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ApiBasePagingItem
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.model.vo.AttachmentItem2
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.picture.PicturePostDataSource
import com.dabenxiang.mimi.view.home.picture.PicturePostFactory
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import com.dabenxiang.mimi.view.home.video.VideoFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CAROUSEL_LIMIT = 5
        const val PAGING_LIMIT = 20
    }

    private var _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private var _tabLayoutPosition = MutableLiveData<Int>()
    val tabLayoutPosition: LiveData<Int> = _tabLayoutPosition

    private var _carouselResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>>()
    val carouselResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>> =
        _carouselResult

    private var _videosResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>>()
    val videosResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>> =
        _videosResult

    private var _clipsResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<MemberPostItem>>>>>()
    val clipsResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<MemberPostItem>>>>> =
        _clipsResult

    private var _pictureResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<MemberPostItem>>>>>()
    val pictureResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<MemberPostItem>>>>> =
        _pictureResult

    private var _clubResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<MemberClubItem>>>>>()
    val clubResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<MemberClubItem>>>>> =
        _clubResult

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private var _attachmentResult2 = MutableLiveData<ApiResult<AttachmentItem2>>()
    val attachmentResult2: LiveData<ApiResult<AttachmentItem2>> = _attachmentResult2

    private var _followClubResult = MutableLiveData<ApiResult<Pair<Int, Boolean>>>()
    val followClubResult: LiveData<ApiResult<Pair<Int, Boolean>>> = _followClubResult

    private val _picturePostItemList = MutableLiveData<PagedList<MemberPostItem>>()
    val picturePostItemList: LiveData<PagedList<MemberPostItem>> = _picturePostItemList

    fun setTopTabPosition(position: Int) {
        if (position != tabLayoutPosition.value) {
            _tabLayoutPosition.value = position
        }
    }

    fun loadNestedStatisticsListForCarousel(position: Int, src: HomeTemplate.Carousel) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().statisticsHomeVideos(
                    isAdult = src.isAdult,
                    offset = 0,
                    limit = CAROUSEL_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _carouselResult.value = Pair(position, it) }
        }
    }

    fun loadNestedStatisticsList(position: Int, src: HomeTemplate.Statistics) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().statisticsHomeVideos(
                    category = src.categories,
                    isAdult = src.isAdult,
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videosResult.value = Pair(position, it) }
        }
    }

    fun loadNestedClipList(position: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersPost(
                    type = PostType.VIDEO,
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clipsResult.value = Pair(position, it) }
        }
    }

    fun loadNestedPictureList(position: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersPost(
                    type = PostType.IMAGE,
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _pictureResult.value = Pair(position, it) }
        }
    }

    fun loadNestedClubList(position: Int) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().getMembersClub(
                    offset = 0,
                    limit = PAGING_LIMIT
                )
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clubResult.value = Pair(position, it) }
        }
    }

    fun getAttachment(id: String, position: Int, type: AttachmentType) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                emit(ApiResult.success(AttachmentItem(id, bitmap, position, type)))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult.value = it }
        }
    }

    fun getAttachment(id: String, parentPosition: Int, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                emit(ApiResult.success(AttachmentItem2(id, bitmap, parentPosition, position)))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult2.value = it }
        }
    }

    fun followClub(id: Int, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followClub(id)
                    else -> apiRepository.cancelFollowClub(id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(Pair(position, isFollow)))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followClubResult.value = it }
        }
    }

    fun getVideos(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            getVideoPagingItems(category, isAdult).asFlow()
                .collect { _videoList.value = it }
        }
    }

    private fun getVideoPagingItems(
        category: String?,
        isAdult: Boolean
    ): LiveData<PagedList<BaseVideoItem>> {
        val videoDataSource = VideoDataSource(
            isAdult,
            category,
            viewModelScope,
            domainManager.getApiRepository(),
            pagingCallback
        )
        val videoFactory = VideoFactory(videoDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(videoFactory, config).build()
    }

    fun getPicturePosts() {
        viewModelScope.launch {
            getPicturePostPagingItems().asFlow()
                .collect { _picturePostItemList.value = it }
        }
    }

    private fun getPicturePostPagingItems(): LiveData<PagedList<MemberPostItem>> {
        val pictureDataSource = PicturePostDataSource(pagingCallback, viewModelScope, domainManager)
        val pictureFactory = PicturePostFactory(pictureDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
    }

    private val pagingCallback = object : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {
        }
    }
}