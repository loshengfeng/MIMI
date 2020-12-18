package com.dabenxiang.mimi.view.club.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.view.club.topic_detail.TopicListFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject
import timber.log.Timber

class ClubItemViewModel : ClubViewModel() {

    var totalCount: Int = 0

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(type: ClubTabItemType) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(type)

    ).flattenMerge(2).buffer().cachedIn(viewModelScope)

    private fun postItems(type: ClubTabItemType) = Pager(
            config = PagingConfig(pageSize = ClubItemMediator.PER_LIMIT),
            remoteMediator = ClubItemMediator(mimiDB, domainManager, adWidth, adHeight,
                    type, pagingCallback)
    ) {
        mimiDB.postDBItemDao().pagingSourceByClubTab( ClubItemMediator::class.simpleName+ type.toString())
    }.flow.map { pagingData->
        pagingData.map {
            it
        }.insertSeparators{ before, after->
            if(after!=null && after.postDBItem.index.rem(TopicListFragment.AD_GAP) == 0 ){
                val adItem = MemberPostWithPostDBItem(after.postDBItem, after.memberPostItem)
                adItem.apply {
                    postDBItem.id = (1024..1024*10).random().toLong()
                    postDBItem.postType = PostType.AD
                    postDBItem.timestamp = after.postDBItem.timestamp+1
                    memberPostItem = MemberPostItem(type = PostType.AD, adItem = adResult.value)
                }
            }else {
                null
            }
        }
    }



}