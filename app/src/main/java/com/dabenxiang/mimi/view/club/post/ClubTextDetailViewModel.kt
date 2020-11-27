package com.dabenxiang.mimi.view.club.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.event.SingleLiveEvent
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.enums.LikeType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentDataSource
import com.dabenxiang.mimi.view.player.NestedCommentNode
import com.dabenxiang.mimi.view.player.RootCommentNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.*

class ClubTextDetailViewModel: BaseViewModel() {

    private var _postDetailResult = MutableLiveData<ApiResult<ApiBaseItem<MemberPostItem>>>()
    val postDetailResult: LiveData<ApiResult<ApiBaseItem<MemberPostItem>>> = _postDetailResult

    private var _currentCommentType = CommentType.NEWEST
    val currentCommentType: CommentType
        get() = _currentCommentType

    private val _followResult = MutableLiveData<ApiResult<Nothing>>()
    val followResult: LiveData<ApiResult<Nothing>> = _followResult

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    private val _replyCommentResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val replyCommentResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _replyCommentResult

    private val _commentLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val commentLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _commentLikeResult

    private val _commentDeleteLikeResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val commentDeleteLikeResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> =
        _commentDeleteLikeResult

    private val _postCommentResult = MutableLiveData<SingleLiveEvent<ApiResult<Nothing>>>()
    val postCommentResult: LiveData<SingleLiveEvent<ApiResult<Nothing>>> = _postCommentResult

    fun getPostDetail(item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val result = apiRepository.getMemberPostDetail(item.id)
                if (!result.isSuccessful) throw HttpException(result)
                emit(ApiResult.success(result.body()))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _postDetailResult.value = it }
        }
    }

    fun getCommentInfo(postId: Long, commentType: CommentType, adapter: CommentAdapter) {
        viewModelScope.launch {
            _currentCommentType = commentType
            val dataSrc = CommentDataSource(postId, commentType.value, domainManager)
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

    fun favoritePost(item: MemberPostItem, isFavorite: Boolean, update: (Boolean, Int) -> Unit) {
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
                emit(ApiResult.success(item.favoriteCount))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(isFavorite, it.result)
                        }
                    }
                }
        }
    }

    fun likePost(item: MemberPostItem, isLike: Boolean, type: LikeType, originType: LikeType?, update: (Boolean, MemberPostItem) -> Unit) {
        viewModelScope.launch {
            var like = isLike
            flow {
                val apiRepository = domainManager.getApiRepository()
                if (isLike) {
                    val request = LikeRequest(type)
                    val result = apiRepository.like(item.id, request)
                    if (!result.isSuccessful) throw HttpException(result)
                    item.likeType = type

                    if (type == LikeType.LIKE) {
                        item.likeCount += 1
                    } else {
                        item.dislikeCount += 1
                    }
                    emit(ApiResult.success(item))
                } else {
                    if (originType == type) {
                        val result = apiRepository.deleteLike(item.id)
                        if (!result.isSuccessful) throw HttpException(result)

                        item.likeType = null

                        if (type == LikeType.LIKE) {
                            item.likeCount -= 1
                        } else {
                            item.dislikeCount -= 1
                        }
                    } else if (originType == LikeType.LIKE) {
                        val request = LikeRequest(LikeType.DISLIKE)
                        val result = apiRepository.like(item.id, request)
                        if (!result.isSuccessful) throw HttpException(result)

                        like = true
                        item.likeType = LikeType.DISLIKE
                        item.likeCount -= 1
                        item.dislikeCount += 1
                    } else if (originType == LikeType.DISLIKE) {
                        val request = LikeRequest(LikeType.DISLIKE)
                        val result = apiRepository.like(item.id, request)
                        if (!result.isSuccessful) throw HttpException(result)

                        like = true
                        item.likeType = LikeType.LIKE
                        item.likeCount += 1
                        item.dislikeCount -=1
                    }
                    emit(ApiResult.success(item))
                }
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect {
                    when (it) {
                        is ApiResult.Success -> {
                            update(like, it.result)
//                            getAllOtherPosts(lastPosition)
                        }
                    }
                }
        }
    }

//    fun likePost(item: MemberPostItem, isLike: Boolean, update: (Boolean, Int) -> Unit) {
//        Log.d("arvin", "isLike : " + isLike)
//        viewModelScope.launch {
//            flow {
//                val apiRepository = domainManager.getApiRepository()
//                val likeType = when {
//                    isLike -> LikeType.LIKE
//                    else -> LikeType.DISLIKE
//                }
//                val request = LikeRequest(likeType)
//                val result = apiRepository.like(item.id, request)
//                if (!result.isSuccessful) throw HttpException(result)
//
////                item.likeType = likeType
//                item.likeCount = when (item.likeType) {
//                    LikeType.LIKE -> item.likeCount + 1
//                    else -> item.likeCount - 1
//                }
//
//                emit(ApiResult.success(item.likeCount))
//            }
//                .flowOn(Dispatchers.IO)
//                .onStart { emit(ApiResult.loading()) }
//                .onCompletion { emit(ApiResult.loaded()) }
//                .catch { e -> emit(ApiResult.error(e)) }
//                .collect {
//                    when (it) {
//                        is ApiResult.Success -> {
//                            update(isLike, it.result)
////                            getAllOtherPosts(lastPosition)
//                        }
//                    }
//                }
//        }
//    }

    fun followMember(
        item: MemberPostItem,
        items: ArrayList<MemberPostItem>,
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
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _followResult.value = it }
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

    fun getReplyComment(parentNode: RootCommentNode, item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                var isFirst = true
                var total = 0L
                var offset = 0L
                var currentSize = 0
                while (isFirst || replyCommentHasNextPage(total, offset, currentSize)) {
                    isFirst = false
                    currentSize = 0

                    val apiRepository = domainManager.getApiRepository()
                    val response = apiRepository.getMembersPostComment(
                        postId = item.id,
                        parentId = parentNode.data.id,
                        sorting = 1,
                        offset = "0",
                        limit = "50"
                    )
                    if (!response.isSuccessful) throw HttpException(response)

                    parentNode.nestedCommentList.clear()
                    response.body()?.content?.map {
                        NestedCommentNode(parentNode, it)
                    }?.also {
                        parentNode.nestedCommentList.addAll(it)
                        val pagingItem = response.body()?.paging
                        total = pagingItem?.count ?: 0L
                        offset = pagingItem?.offset ?: 0L
                        currentSize = it.size
                    }

                    if (currentSize == 0) {
                        break
                    }
                }
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .collect { _replyCommentResult.value = SingleLiveEvent(it) }
        }
    }

    fun postCommentLike(commentId: Long, type: LikeType, item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                val request = PostLikeRequest(type.value)
                val apiRepository = domainManager.getApiRepository()
                val resp = apiRepository.postMembersPostCommentLike(item.id, commentId, request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _commentLikeResult.value = SingleLiveEvent(it) }
        }
    }

    fun deleteCommentLike(commentId: Long, item: MemberPostItem) {
        viewModelScope.launch {
            flow {
                val apiRepository = domainManager.getApiRepository()
                val resp = apiRepository.deleteMembersPostCommentLike(item.id, commentId)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .onStart { emit(ApiResult.loading()) }
                .catch { e -> emit(ApiResult.error(e)) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _commentDeleteLikeResult.value = SingleLiveEvent(it) }
        }
    }

    fun postComment(postId: Long, replyId: Long?, comment: String) {
        viewModelScope.launch {
            flow {
                val request = PostCommentRequest(replyId, comment)
                val resp = domainManager.getApiRepository().postMembersPostComment(postId, request)
                if (!resp.isSuccessful) throw HttpException(resp)
                emit(ApiResult.success(null))
            }
                .flowOn(Dispatchers.IO)
                .catch { e -> emit(ApiResult.error(e)) }
                .onStart { emit(ApiResult.loading()) }
                .onCompletion { emit(ApiResult.loaded()) }
                .collect { _postCommentResult.value = SingleLiveEvent(it) }
        }
    }

    private fun replyCommentHasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < 50 -> false
            offset >= total -> false
            else -> true
        }
    }
}