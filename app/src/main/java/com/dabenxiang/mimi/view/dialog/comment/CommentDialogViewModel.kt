package com.dabenxiang.mimi.view.dialog.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.event.SingleLiveEvent
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.api.vo.PostCommentRequest
import com.dabenxiang.mimi.model.api.vo.PostLikeRequest
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentDataSource
import com.dabenxiang.mimi.view.player.NestedCommentNode
import com.dabenxiang.mimi.view.player.RootCommentNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class CommentDialogViewModel: BaseViewModel() {

    private val _apiLoadReplyCommentResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiLoadReplyCommentResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _apiLoadReplyCommentResult

    private val _apiPostCommentResult = MutableLiveData<SingleLiveEvent<ApiResult<MembersPostCommentItem>>>()
    val apiPostCommentResult: LiveData<SingleLiveEvent<ApiResult<MembersPostCommentItem>>> = _apiPostCommentResult

    private val _apiCommentLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiCommentLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _apiCommentLikeResult

    private val _apiDeleteCommentLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val apiDeleteCommentLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _apiDeleteCommentLikeResult

    fun setupCommentDataSource(postId: Long, adapter: CommentAdapter) {
        viewModelScope.launch {
            val dataSrc = CommentDataSource(postId, 1, domainManager)
            dataSrc.loadMore().also { load ->
                withContext(Dispatchers.Main) {
                    load.content?.let { list ->
                        val finalList = list.map { item ->
                            RootCommentNode(item)
                        }
                        adapter.setList(finalList)
                    }
                }
                setupLoadMoreResult(adapter, load.isEnd)
            }

            adapter.loadMoreModule.setOnLoadMoreListener {
                viewModelScope.launch(Dispatchers.IO) {
                    dataSrc.loadMore().also { load ->
                        withContext(Dispatchers.Main) {
                            load.content?.also { list ->
                                val finalList = list.map { item ->
                                    RootCommentNode(item)
                                }
                                adapter.addData(finalList)
                            }
                            setupLoadMoreResult(adapter, load.isEnd)
                        }
                    }
                }
            }
        }
    }

    private fun setupLoadMoreResult(adapter: CommentAdapter, isEnd: Boolean) {
        if (isEnd) {
            adapter.loadMoreModule.loadMoreEnd()
        } else {
            adapter.loadMoreModule.loadMoreComplete()
        }
    }

    fun loadReplyComment(postId: Long, parentNode: RootCommentNode, commentId: Long) {
        viewModelScope.launch {
            flow {
                var isFirst = true
                var total = 0L
                var offset = 0L
                var currentSize = 0
                while (isFirst || replyCommentHasNextPage(total, offset, currentSize)) {
                    isFirst = false

                    currentSize = 0

                    val resp = domainManager.getApiRepository()
                        .getMembersPostComment(
                            postId = postId,
                            parentId = commentId,
                            sorting = 1,
                            offset = "0",
                            limit = "50"
                        )
                    if (!resp.isSuccessful) throw HttpException(resp)

                    parentNode.nestedCommentList.clear()
                    resp.body()?.content?.map {
                        NestedCommentNode(parentNode, it)
                    }?.also {
                        parentNode.nestedCommentList.addAll(it)

                        val pagingItem = resp.body()?.paging
                        total = pagingItem?.count ?: 0L
                        offset = pagingItem?.offset ?: 0L
                        currentSize = it.size
                    }

                    if (currentSize == 0)
                        break
                }

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    Timber.e(e)
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiLoadReplyCommentResult.value = SingleLiveEvent(it)
                }
        }
    }

    private fun replyCommentHasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < 50 -> false
            offset >= total -> false
            else -> true
        }
    }

    fun postCommentLike(postId: Long, commentId: Long, body: PostLikeRequest) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository()
                    .postMembersPostCommentLike(postId, commentId, body)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiCommentLikeResult.value = SingleLiveEvent(it)
                }
        }
    }

    fun deleteCommentLike(postId: Long, commentId: Long) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository()
                    .deleteMembersPostCommentLike(postId, commentId)
                if (!resp.isSuccessful) throw HttpException(resp)

                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiDeleteCommentLikeResult.value = SingleLiveEvent(it)
                }
        }
    }

    fun postComment(videoId: Long, body: PostCommentRequest) {
        viewModelScope.launch {
            flow {
                val resp = domainManager.getApiRepository().postMembersPostComment(videoId, body)
                if (!resp.isSuccessful) throw HttpException(resp)
                val comment = MembersPostCommentItem(
                    content = body.content,
                    creationDate = Date(),
                    creatorId = accountManager.getProfile().userId,
                    postAvatarAttachmentId = accountManager.getProfile().avatarAttachmentId,
                    postName = accountManager.getProfile().friendlyName,
                    commentCount = 0,
                    dislikeCount = 0,
                    id = resp.body()?.content,
                    likeCount = 0,
                    likeType = null,
                    reported = false
                )
                emit(ApiResult.success(comment))
            }
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    emit(ApiResult.error(e))
                }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect {
                    _apiPostCommentResult.value = SingleLiveEvent(it)
                }
        }
    }
}