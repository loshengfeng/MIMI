package com.dabenxiang.mimi.view.home

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.club.ClubDataSource
import com.dabenxiang.mimi.view.home.club.ClubFactory
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import com.dabenxiang.mimi.view.home.memberpost.MemberPostFactory
import com.dabenxiang.mimi.view.home.postfollow.PostFollowDataSource
import com.dabenxiang.mimi.view.home.postfollow.PostFollowFactory
import com.dabenxiang.mimi.view.home.video.VideoDataSource
import com.dabenxiang.mimi.view.home.video.VideoFactory
import com.dabenxiang.mimi.widget.utility.LruCacheUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CAROUSEL_LIMIT = 5
        const val PAGING_LIMIT = 20
    }

    var lastListIndex = 0 // 垂直recycler view 跳出後的最後一筆資料

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

    private var _attachmentByTypeResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentByTypeResult: LiveData<ApiResult<AttachmentItem>> = _attachmentByTypeResult

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private var _followClubResult = MutableLiveData<ApiResult<Pair<Int, Boolean>>>()
    val followClubResult: LiveData<ApiResult<Pair<Int, Boolean>>> = _followClubResult

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private val _postFollowItemListResult = MutableLiveData<PagedList<PostFollowItem>>()
    val postFollowItemListResult: LiveData<PagedList<PostFollowItem>> = _postFollowItemListResult

    private val _clipPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val clipPostItemListResult: LiveData<PagedList<MemberPostItem>> = _clipPostItemListResult

    private val _picturePostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val picturePostItemListResult: LiveData<PagedList<MemberPostItem>> = _picturePostItemListResult

    private val _textPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val textPostItemListResult: LiveData<PagedList<MemberPostItem>> = _textPostItemListResult

    private val _clubItemListResult = MutableLiveData<PagedList<MemberClubItem>>()
    val clubItemListResult: LiveData<PagedList<MemberClubItem>> = _clubItemListResult

    private val _postReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postReportResult: LiveData<ApiResult<Nothing>> = _postReportResult

    private val _scrollToLastPosition = MutableLiveData<Boolean>()
    val scrollToLastPosition: LiveData<Boolean> = _scrollToLastPosition

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
                val resp = domainManager.getApiRepository().getMembersClubPost(
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
                val item = AttachmentItem(
                    id = id,
                    bitmap = bitmap,
                    position = position,
                    type = type
                )
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentByTypeResult.value = it }
        }
    }

    fun getAttachment(id: String, parentPosition: Int, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                    id = id,
                    bitmap = bitmap,
                    parentPosition = parentPosition,
                    position = position
                )
                emit(ApiResult.success(item))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _attachmentResult.value = it }
        }
    }

    fun getBitmap(id: String, update: ((String) -> Unit)) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                LruCacheUtils.putLruCache(id, bitmap)
                emit(ApiResult.success(id))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when(it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    fun followClub(item: MemberClubItem, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followClub(item.id)
                    else -> apiRepository.cancelFollowClub(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(Pair(position, isFollow)))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followClubResult.value = it }
        }
    }

    fun followPost(item: MemberPostItem, position: Int, isFollow: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followPostResult.value = it }
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

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }

                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun getVideos(category: String?, isAdult: Boolean) {
        viewModelScope.launch {
            getVideoPagingItems(category, isAdult).asFlow()
                .collect { _videoList.value = it }
        }
    }


    fun sendPostReport(item: MemberPostItem, content: String) {
        viewModelScope.launch {
            flow {
                val request = ReportRequest(content)
                val result = domainManager.getApiRepository().sendPostReport(item.id, request)
                if (!result.isSuccessful) throw HttpException(result)
                item.reported = true
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postReportResult.value = it }
        }
    }

    fun getPostFollows() {
        viewModelScope.launch {
            getPostFollowPagingItems().asFlow()
                .collect { _postFollowItemListResult.value = it }
        }
    }

    fun getClipPosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.VIDEO).asFlow()
                .collect { _clipPostItemListResult.value = it }
        }
    }

    fun getPicturePosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.IMAGE).asFlow()
                .collect { _picturePostItemListResult.value = it }
        }
    }

    fun getTextPosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.TEXT).asFlow()
                .collect { _textPostItemListResult.value = it }
        }
    }

    private fun getPostFollowPagingItems(): LiveData<PagedList<PostFollowItem>> {
        val postFollowDataSource =
            PostFollowDataSource(pagingCallback, viewModelScope, domainManager)
        val pictureFactory = PostFollowFactory(postFollowDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
    }

    private fun getMemberPostPagingItems(postType: PostType): LiveData<PagedList<MemberPostItem>> {
        val pictureDataSource =
            MemberPostDataSource(pagingCallback, viewModelScope, domainManager, postType)
        val pictureFactory = MemberPostFactory(pictureDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
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

    fun getClubs() {
        viewModelScope.launch {
            getClubPagingItems().asFlow()
                .collect { _clubItemListResult.value = it }
        }
    }

    private fun getClubPagingItems(): LiveData<PagedList<MemberClubItem>> {
        val clubDataSource = ClubDataSource(pagingCallback, viewModelScope, domainManager)
        val clubFactory = ClubFactory(clubDataSource)
        val config = PagedList.Config.Builder().setPrefetchDistance(4).build()
        return LivePagedListBuilder(clubFactory, config).build()
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

        override fun onSucceed() {
            _scrollToLastPosition.postValue(true)
        }
    }
}