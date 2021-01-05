package com.dabenxiang.mimi.view.club.pages

import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.view.club.pages.ClubItemMediator.Companion.AD_GAP
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber

class ClubItemViewModel : ClubViewModel() {

    var totalCount: Int = 0

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(pageCode:String, type: ClubTabItemType) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(pageCode, type)

    ).flattenMerge(2).cachedIn(viewModelScope)

    private fun postItems(pageCode:String, type: ClubTabItemType) = Pager(
            config = PagingConfig(pageSize = ClubItemMediator.PER_LIMIT),
            remoteMediator = ClubItemMediator(mimiDB, domainManager, adWidth, adHeight, pageCode,
                    type, getAdCode(type), pagingCallback)
    ) {
        mimiDB.postDBItemDao()
            .pagingSourceByPageCode(pageCode)
    }.flow.map { pagingData ->
        pagingData.map {
           it.memberPostItem
        }
     }

    fun getAdCode(type: ClubTabItemType): String {
        return when (type) {
            ClubTabItemType.FOLLOW -> "subscribe"
            ClubTabItemType.HOTTEST -> "recommend"
            ClubTabItemType.LATEST -> "news"
            ClubTabItemType.SHORT_VIDEO -> "video"
            ClubTabItemType.PICTURE -> "image"
            ClubTabItemType.NOVEL -> "text"
        }
    }

}