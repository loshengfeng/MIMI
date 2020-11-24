package com.dabenxiang.mimi.view.clip

import ClipPagingSource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.blankj.utilcode.util.FileIOUtils
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.CategoryType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import com.dabenxiang.mimi.view.home.memberpost.MemberPostFactory
import com.dabenxiang.mimi.view.order.OrderPagingSource
import com.dabenxiang.mimi.widget.utility.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File

class ClipViewModel : BaseViewModel() {

    private var _clipResult = MutableLiveData<ApiResult<Triple<String, Int, File>>>()
    val clipResult: LiveData<ApiResult<Triple<String, Int, File>>> = _clipResult

    private var _followResult = MutableLiveData<ApiResult<Int>>()
    val followResult: LiveData<ApiResult<Int>> = _followResult

    private var _favoriteResult = MutableLiveData<ApiResult<Int>>()
    val favoriteResult: LiveData<ApiResult<Int>> = _favoriteResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private var _postDetailResult = MutableLiveData<ApiResult<Int>>()
    val postDetailResult: LiveData<ApiResult<Int>> = _postDetailResult

    private val _videoReport = MutableLiveData<ApiResult<Nothing>>()
    val videoReport: LiveData<ApiResult<Nothing>> = _videoReport

    private val _clipPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val clipPostItemListResult: LiveData<PagedList<MemberPostItem>> = _clipPostItemListResult

    fun getClip(id: String, pos: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteStream = result.body()?.byteStream()
                val filename = result.headers()["content-disposition"]
                    ?.split("; ")
                    ?.takeIf { it.size >= 2 }
                    ?.run { this[1].split("=") }
                    ?.takeIf { it.size >= 2 }
                    ?.run { this[1] }
                    ?: "$id.mov"
                val file = FileUtil.getClipFile(filename)
                FileIOUtils.writeFileFromIS(file, byteStream)

                emit(ApiResult.success(Triple(id, pos, file)))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _clipResult.value = it }
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
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
        }
    }

    fun getPostDetail(item: MemberPostItem, position: Int, update: (Int, Boolean) -> Unit) {
        viewModelScope.launch {
            flow {
                /** for debug **/
                if (isLogin()) domainManager.getApiRepository().getMe()
                else domainManager.getApiRepository().getGuestInfo()
                /**-----------**/
                val result = domainManager.getApiRepository().getMemberPostDetail(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                val deducted = result.body()?.content?.deducted ?: false
                emit(deducted)
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    e.printStackTrace()
                    update(position, false)
                }
                .collect { update(position, it) }
        }
    }

    fun favoritePost(item: MemberPostItem, position: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = when {
                    isFavorite -> apiRepository.addFavorite(item.id)
                    else -> apiRepository.deleteFavorite(item.id)
                }
                if (!result.isSuccessful) throw HttpException(result)
                item.isFavorite = isFavorite
                if (isFavorite) item.favoriteCount++ else item.favoriteCount--
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

                item.likeType = likeType
                item.likeCount = when (item.likeType) {
                    LikeType.LIKE -> item.likeCount + 1
                    else -> item.likeCount - 1
                }

                emit(ApiResult.success(position))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _likePostResult.value = it }
        }
    }

    fun sendVideoReport(id: String, error: String){
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getMemberVideoReport(
                    videoId= id.toLong(), type = PostType.VIDEO.value)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _videoReport.value = it }
        }

    }

    fun getClips(): Flow<PagingData<MemberPostItem>> {
        return Pager(
            config = PagingConfig(pageSize = ApiRepository.NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { ClipPagingSource(domainManager) }
        ).flow.cachedIn(viewModelScope)
    }

    fun getClipPosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.VIDEO).asFlow()
                .collect { _clipPostItemListResult.value = it }
        }
    }

    private fun getMemberPostPagingItems(postType: PostType): LiveData<PagedList<MemberPostItem>> {
        val pictureDataSource =
            MemberPostDataSource(
                HomePagingCallBack(CategoryType.valueOf(postType.name)),
                viewModelScope,
                domainManager,
                postType,
                0,
                0
            )
        val pictureFactory = MemberPostFactory(pictureDataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(pictureFactory, config).build()
    }

    inner class HomePagingCallBack(private val type: CategoryType) : PagingCallback {
        override fun onLoading() {
            setShowProgress(true)
        }

        override fun onLoaded() {
            setShowProgress(false)
        }

        override fun onThrowable(throwable: Throwable) {

        }

        override fun onTotalCount(count: Long) {
//            _totalCountResult.postValue(Pair(type, count.toInt()))
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
//            totalCount = if (isInitial) count.toInt()
//            else totalCount.plus(count.toInt())
//            if (isInitial) cleanRemovedPosList()
        }
    }
}
