package com.dabenxiang.mimi.view.club.topic_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.club.pages.ClubItemMediator
import com.dabenxiang.mimi.view.club.topic_detail.TopicListFragment.Companion.AD_CODE
import com.dabenxiang.mimi.view.club.topic_detail.TopicListFragment.Companion.AD_GAP
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber

class TopicListViewModel : ClubViewModel() {

    private val _cleanResult = MutableLiveData<ApiResult<Nothing>>()
    val cleanResult: LiveData<ApiResult<Nothing>> = _cleanResult

    private val _deleteFavorites = MutableLiveData<Int>()
    val deleteFavorites: LiveData<Int> = _deleteFavorites

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(pageCode: String, tag: String, orderBy: OrderBy) = postItems(pageCode, tag, orderBy).cachedIn(viewModelScope)

    private fun postItems(pageCode: String, tag: String, orderBy: OrderBy) = Pager(
            config = PagingConfig(pageSize = TopicPostMediator.PER_LIMIT),
            remoteMediator = TopicPostMediator(
                    mimiDB,
                    pagingCallback,
                    domainManager,
                    pageCode,
                    AD_CODE,
                    tag,
                    orderBy,
                    adWidth,
                    adHeight
            )
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode(pageCode)
    }.flow.map { pagingData ->
        pagingData.map {
            it.memberPostItem
        }
    }
    
}