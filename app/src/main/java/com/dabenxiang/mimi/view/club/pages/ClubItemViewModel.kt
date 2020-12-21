package com.dabenxiang.mimi.view.club.pages

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.club.topic_detail.TopicListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

                val adItem = MemberPostItem(id= (1..2147483647).random().toLong(),
                    type = PostType.AD, adItem = adResult.value)

                val postDBItem =PostDBItem(
                    id= adItem.id,
                    postDBId =  adItem.id,
                    postType = PostType.AD,
                    timestamp = after.postDBItem.timestamp - 1,
                    pageName = after.postDBItem.pageName,
                    index = 0
                )
                MemberPostWithPostDBItem(postDBItem, adItem)
            }else {
                null
            }
        }
    }



}