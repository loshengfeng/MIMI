package com.dabenxiang.mimi.view.club.topic_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class TopicListViewModel : ClubViewModel() {

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _deleteFavorites = MutableLiveData<Int>()
    val deleteFavorites: LiveData<Int> = _deleteFavorites

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(tag: String, orderBy: OrderBy) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(tag, orderBy)

    ).flattenMerge(2).buffer().cachedIn(viewModelScope)

    private fun postItems(tag: String, orderBy: OrderBy) = Pager(
            config = PagingConfig(pageSize = TopicPostMediator.PER_LIMIT),
            remoteMediator = TopicPostMediator(
                    mimiDB,
                    pagingCallback,
                    domainManager,
                    tag,
                    orderBy,
                    adWidth,
                    adHeight
            )
    ) {
        mimiDB.postDBItemDao().pagingSourceByClubTab( TopicPostMediator::class.simpleName+tag+orderBy.toString())
    }.flow



//    fun getData(adapter:TopicListAdapter, tag: String, orderBy: OrderBy) {
//
//        Timber.i("getData")
//        CoroutineScope(Dispatchers.IO).launch {
//            adapter.submitData(PagingData.empty())
//            getPostItemList(tag, orderBy)
//                    .collectLatest {
//                        adapter.submitData(it)
//                    }
//        }
//    }
//
//    private fun getPostItemList(tag: String, orderBy: OrderBy): Flow<PagingData<MemberPostItem>> {
//        return Pager(
//                config = PagingConfig(pageSize = TopicPostDataSource.PER_LIMIT.toInt()),
//                pagingSourceFactory = {
//                    TopicPostDataSource(
//                            pagingCallback,
//                            domainManager,
//                            tag,
//                            orderBy,
//                            adWidth,
//                            adHeight
//                    )
//                }
//        )
//                .flow
//                .onStart {  setShowProgress(true) }
//                .onCompletion { setShowProgress(false) }
//                .cachedIn(viewModelScope)
//    }

}