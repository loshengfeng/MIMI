package com.dabenxiang.mimi.view.club.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ApiBaseItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CommentType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.player.CommentAdapter
import com.dabenxiang.mimi.view.player.CommentDataSource
import com.dabenxiang.mimi.view.player.RootCommentNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ClubTextDetailViewModel: BaseViewModel() {

    private var _postDetailResult = MutableLiveData<ApiResult<ApiBaseItem<MemberPostItem>>>()
    val postDetailResult: LiveData<ApiResult<ApiBaseItem<MemberPostItem>>> = _postDetailResult

    private var _currentCommentType = CommentType.NEWEST
    val currentCommentType: CommentType
        get() = _currentCommentType

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
                        val finalList = list.map { item -> RootCommentNode(item) }
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