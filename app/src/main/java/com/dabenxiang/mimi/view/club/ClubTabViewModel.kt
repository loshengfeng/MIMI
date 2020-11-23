package com.dabenxiang.mimi.view.club

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.CategoryType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseViewModel
import com.dabenxiang.mimi.view.club.topic.TopicListDataSource
import com.dabenxiang.mimi.view.home.memberpost.MemberPostDataSource
import com.dabenxiang.mimi.view.home.memberpost.MemberPostFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class ClubViewModel : BaseViewModel() {

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

    private val _clubCount = MutableLiveData<Int>()
    val clubCount: LiveData<Int> = _clubCount

    fun getClubItemList(): Flow<PagingData<MemberClubItem>> {
        Timber.i("ClubTabFragment getClubItemList")
        return Pager(
            config = PagingConfig(pageSize = TopicListDataSource.PER_LIMIT.toInt()),
            pagingSourceFactory = {
                TopicListDataSource(
                    domainManager,
                    pagingCallback
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    private val pagingCallback = object : PagingCallback {
        override fun onTotalCount(count: Long) {
            _clubCount.postValue(count.toInt())
        }

    }

}