package com.dabenxiang.mimi.view.search.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LikeRequest
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.ReportRequest
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.search.post.keyword.SearchPostByKeywordDataSource
import com.dabenxiang.mimi.view.search.post.keyword.SearchPostByKeywordFactory
import com.dabenxiang.mimi.view.search.post.tag.SearchPostByTagDataSource
import com.dabenxiang.mimi.view.search.post.tag.SearchPostByTagFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SearchPostViewModel : BaseViewModel() {

    private val _postReportResult = MutableLiveData<ApiResult<Nothing>>()
    val postReportResult: LiveData<ApiResult<Nothing>> = _postReportResult

    private val _searchPostItemByTagListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val searchPostItemByTagListResult: LiveData<PagedList<MemberPostItem>> =
        _searchPostItemByTagListResult

    private val _searchPostItemByKeywordListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val searchPostItemByKeywordListResult: LiveData<PagedList<MemberPostItem>> =
        _searchPostItemByKeywordListResult

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    private var _likePostResult = MutableLiveData<ApiResult<Int>>()
    val likePostResult: LiveData<ApiResult<Int>> = _likePostResult

    private val _searchTotalCount = MutableLiveData<Long>()
    val searchTotalCount: LiveData<Long> = _searchTotalCount

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

    fun getSearchPostsByTag(type: PostType, tag: String, isPostFollow: Boolean) {
        viewModelScope.launch {
            getSearchPostByTagPagingItems(type, tag, isPostFollow).asFlow()
                .collect { _searchPostItemByTagListResult.value = it }
        }
    }

    fun getSearchPostsByKeyword(type: PostType, keyword: String, isPostFollow: Boolean) {
        viewModelScope.launch {
            getSearchPostByKeywordPagingItems(type, keyword, isPostFollow).asFlow()
                .collect { _searchPostItemByKeywordListResult.value = it }
        }
    }

    private fun getSearchPostByTagPagingItems(
        type: PostType,
        tag: String,
        isPostFollow: Boolean
    ): LiveData<PagedList<MemberPostItem>> {
        val dataSource = SearchPostByTagDataSource(
            pagingCallback,
            viewModelScope,
            domainManager,
            type,
            tag,
            isPostFollow
        )
        val factory = SearchPostByTagFactory(dataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(factory, config).build()
    }

    private fun getSearchPostByKeywordPagingItems(
        type: PostType,
        tag: String,
        isPostFollow: Boolean
    ): LiveData<PagedList<MemberPostItem>> {
        val dataSource = SearchPostByKeywordDataSource(
            pagingCallback,
            viewModelScope,
            domainManager,
            type,
            tag,
            isPostFollow
        )
        val factory = SearchPostByKeywordFactory(dataSource)
        val config = PagedList.Config.Builder()
            .setPrefetchDistance(4)
            .build()
        return LivePagedListBuilder(factory, config).build()
    }

    private val pagingCallback = object : SearchPagingCallback {
        override fun onTotalCount(count: Long) {
            _searchTotalCount.postValue(count)
        }

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