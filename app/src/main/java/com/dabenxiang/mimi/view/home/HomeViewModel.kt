package com.dabenxiang.mimi.view.home

import android.content.Context
import android.net.Uri
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
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.vo.UploadPicItem
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
import com.dabenxiang.mimi.widget.utility.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.net.URLEncoder

class HomeViewModel : BaseViewModel() {

    companion object {
        const val CAROUSEL_LIMIT = 5
        const val PAGING_LIMIT = 20
        const val TYPE_PIC = "type_pic"
        const val TYPE_COVER = "type_cover"
        const val TYPE_VIDEO = "type_video"
    }

    var adWidth = 0
    var adHeight = 0

    var lastListIndex = 0 // 垂直recycler view 跳出後的最後一筆資料

    private var _videoList = MutableLiveData<PagedList<BaseVideoItem>>()
    val videoList: LiveData<PagedList<BaseVideoItem>> = _videoList

    private var _carouselResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>>()
    val carouselResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>> =
        _carouselResult

    private var _videosResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>>()
    val videosResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<List<StatisticsItem>>>>> =
        _videosResult

    private var _clipsResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>>()
    val clipsResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>> =
        _clipsResult

    private var _pictureResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>>()
    val pictureResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberPostItem>>>>> =
        _pictureResult

    private var _clubResult =
        MutableLiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberClubItem>>>>>()
    val clubResult: LiveData<Pair<Int, ApiResult<ApiBasePagingItem<ArrayList<MemberClubItem>>>>> =
        _clubResult

    private val _postFollowItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val postFollowItemListResult: LiveData<PagedList<MemberPostItem>> = _postFollowItemListResult

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

    private val _postPicResult = MutableLiveData<ApiResult<Long>>()
    val postPicResult: LiveData<ApiResult<Long>> = _postPicResult

    private val _postCoverResult = MutableLiveData<ApiResult<Long>>()
    val postCoverResult: LiveData<ApiResult<Long>> = _postCoverResult

    private val _postVideoResult = MutableLiveData<ApiResult<Long>>()
    val postVideoResult: LiveData<ApiResult<Long>> = _postVideoResult

    private val _uploadPicItem = MutableLiveData<UploadPicItem>()
    val uploadPicItem: LiveData<UploadPicItem> = _uploadPicItem

    private val _uploadCoverItem = MutableLiveData<PicParameter>()
    val uploadCoverItem: LiveData<PicParameter> = _uploadCoverItem

    private val _postVideoMemberResult = MutableLiveData<ApiResult<Long>>()
    val postVideoMemberResult: LiveData<ApiResult<Long>> = _postVideoMemberResult

    private val _totalCountResult = MutableLiveData<Int>()
    val totalCountResult: LiveData<Int> = _totalCountResult

    private val _postArticleResult = MutableLiveData<ApiResult<Long>>()
    val postArticleResult: LiveData<ApiResult<Long>> = _postArticleResult

    private var job = Job()

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
                    when (it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    fun clubFollow(item: MemberClubItem, isFollow: Boolean, update: ((Boolean) -> Unit)) {
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
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(it.result)
                        }
                    }
                }
        }
    }

    fun followMember(item: MemberPostItem, isFollow: Boolean, update: (Boolean) -> Unit) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFollow -> apiRepository.followPost(item.creatorId)
                    else -> apiRepository.cancelFollowPost(item.creatorId)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFollow = isFollow
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Empty -> {
                            update(isFollow)
                        }
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

    private fun getPostFollowPagingItems(): LiveData<PagedList<MemberPostItem>> {
        val postFollowDataSource =
            PostFollowDataSource(pagingCallback, viewModelScope, domainManager, adWidth, adHeight)
        val pictureFactory = PostFollowFactory(postFollowDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
    }

    private fun getMemberPostPagingItems(postType: PostType): LiveData<PagedList<MemberPostItem>> {
        val pictureDataSource =
            MemberPostDataSource(
                pagingCallback,
                viewModelScope,
                domainManager,
                postType,
                adWidth,
                adHeight
            )
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
            isAdult, category, viewModelScope, domainManager, pagingCallback, adWidth, adHeight
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
        val clubDataSource = ClubDataSource(
            pagingCallback, viewModelScope, domainManager, adWidth, adHeight
        )
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

        override fun onTotalCount(count: Long) {
            _totalCountResult.postValue(count.toInt())
        }
    }

    fun postAttachment(pic: String, context: Context, type: String) {
        viewModelScope.launch(context = job) {
            flow {
                val realPath = UriUtils.getPath(context, Uri.parse(pic))
                val fileNameSplit = realPath?.split("/")
                val fileName = fileNameSplit?.last()
                val extSplit = fileName?.split(".")
                val ext = "." + extSplit?.last()

                if (type == TYPE_PIC) {
                    val uploadPicItem = UploadPicItem(ext = ext)
                    _uploadPicItem.postValue(uploadPicItem)
                } else if (type == TYPE_COVER) {
                    val picParameter = PicParameter(ext = ext)
                    _uploadCoverItem.postValue(picParameter)
                }

                Timber.d("Upload photo path : $realPath")
                Timber.d("Upload photo ext : $ext")

                val result = domainManager.getApiRepository().postAttachment(
                    File(realPath),
                    fileName = URLEncoder.encode(fileName, "UTF-8")
                )

                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (type) {
                        TYPE_PIC -> _postPicResult.postValue(it)
                        TYPE_COVER -> _postCoverResult.postValue(it)
                        TYPE_VIDEO -> _postVideoResult.postValue(it)
                    }
                }
        }
    }

    fun postPic(request: PostMemberRequest, content: String) {
        viewModelScope.launch(context = job) {
            flow {
                request.content = content
                Timber.d("Post member request : $request")
                val resp = domainManager.getApiRepository().postMembersPost(request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postVideoMemberResult.value = it }
        }
    }

    fun postArticle(title: String, content: String, tags: ArrayList<String>) {
        viewModelScope.launch {
            flow {
                val request = PostMemberRequest(
                    title = title,
                    content = content,
                    type = PostType.TEXT.value,
                    tags = tags
                )

                val resp = domainManager.getApiRepository().postMembersPost(request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(resp.body()?.content))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postArticleResult.value = it }
        }
    }

    fun cancelJob() {
        job.cancel()
    }

    fun clearLiveDataValue() {
        _postPicResult.value = null
        _postCoverResult.value = null
        _postVideoResult.value = null
    }
}