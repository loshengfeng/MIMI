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
    fun posts(type: ClubTabItemType) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(type)

    ).flattenMerge(2).cachedIn(viewModelScope)

    private fun postItems(type: ClubTabItemType) = Pager(
            config = PagingConfig(pageSize = ClubItemMediator.PER_LIMIT),
            remoteMediator = ClubItemMediator(mimiDB, domainManager, adWidth, adHeight,
                    type, getAdCode(type), pagingCallback)
    ) {
        mimiDB.postDBItemDao().pagingSourceByPageCode( ClubItemMediator::class.simpleName+ type.toString())


    }.flow.map { pagingData->
        val adItems = (mimiDB.postDBItemDao().getPostDBItemsByTime(getAdCode(type)) as ArrayList<MemberPostWithPostDBItem>)
        Timber.i("adPostItem adItems =$adItems")
        pagingData.map {
            it
        }.insertSeparators{ before, after->
            if(before!=null && before.postDBItem.index >=AD_GAP && before.postDBItem.index.rem(AD_GAP) == 0 ){
                getAdItem(adItems, before)
            }else {
                null
            }
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

    private fun getAdItem(adItems: ArrayList<MemberPostWithPostDBItem>, before:MemberPostWithPostDBItem): MemberPostWithPostDBItem? {
        return if (adItems.isEmpty()) {
            val adItem = MemberPostItem(id= (1..2147483647).random().toLong(),
                type = PostType.AD, adItem = AdItem()
            )

            val postDBItem =PostDBItem(
                id= adItem.id,
                postDBId =  adItem.id,
                postType = PostType.AD,
                timestamp = before.postDBItem.timestamp + 1,
                pageCode = before.postDBItem.pageCode,
                index = 0
            )
            MemberPostWithPostDBItem(postDBItem, adItem)

        }
        else adItems.removeFirst()
    }

}