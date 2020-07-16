package com.dabenxiang.mimi.view.picturedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ImageUtils
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.model.vo.AttachmentItem
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentDataSource
import com.dabenxiang.mimi.view.player.RootCommentNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class PictureDetailViewModel : BaseViewModel() {

    private var _attachmentResult = MutableLiveData<ApiResult<AttachmentItem>>()
    val attachmentResult: LiveData<ApiResult<AttachmentItem>> = _attachmentResult

    private var _followPostResult = MutableLiveData<ApiResult<Int>>()
    val followPostResult: LiveData<ApiResult<Int>> = _followPostResult

    fun getAttachment(id: String, position: Int) {
        viewModelScope.launch {
            flow {
                val result = domainManager.getApiRepository().getAttachment(id)
                if (!result.isSuccessful) throw HttpException(result)
                val byteArray = result.body()?.bytes()
                val bitmap = ImageUtils.bytes2Bitmap(byteArray)
                val item = AttachmentItem(
                    id = id,
                    bitmap = bitmap,
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

    fun getCommentInfo(postId: Long, commentType: CommentType, adapter: CommentAdapter) {
        viewModelScope.launch {
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

}