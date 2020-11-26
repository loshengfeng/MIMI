package com.dabenxiang.mimi.view.club.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CategoryType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import com.dabenxiang.mimi.view.home.memberpost.MemberPostFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ClubShortVideoViewModel : BaseViewModel() {

    private val _textPostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val textPostItemListResult: LiveData<PagedList<MemberPostItem>> = _textPostItemListResult

    private val _totalCountResult = MutableLiveData<Pair<CategoryType,Int>>()
    val totalCountResult: LiveData<Pair<CategoryType,Int>> = _totalCountResult

    private val _picturePostItemListResult = MutableLiveData<PagedList<MemberPostItem>>()
    val picturePostItemListResult: LiveData<PagedList<MemberPostItem>> = _picturePostItemListResult

    var totalCount: Int = 0

    fun getTextPosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.TEXT).asFlow()
                .collect { _textPostItemListResult.value = it }
        }
    }

    fun getPicturePosts() {
        viewModelScope.launch {
            getMemberPostPagingItems(PostType.IMAGE).asFlow()
                .collect { _picturePostItemListResult.value = it }
        }
    }

    private fun getMemberPostPagingItems(postType: PostType): LiveData<PagedList<MemberPostItem>> {
        val pictureDataSource =
            MemberPostDataSource(
                HomePagingCallBack(CategoryType.valueOf(postType.name)),
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
            _totalCountResult.postValue(Pair(type, count.toInt()))
        }

        override fun onCurrentItemCount(count: Long, isInitial: Boolean) {
            totalCount = if (isInitial) count.toInt()
            else totalCount.plus(count.toInt())
            if (isInitial) cleanRemovedPosList()
        }
    }
}