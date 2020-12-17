package com.dabenxiang.mimi.view.club.pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.club.base.ClubViewModel
import com.dabenxiang.mimi.model.db.MiMiDB
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.koin.core.component.inject
import timber.log.Timber

class ClubItemViewModel : ClubViewModel() {

    private val _postCount = MutableLiveData<Int>()
    val postCount: LiveData<Int> = _postCount

    var totalCount: Int = 0

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun posts(type: ClubTabItemType) = flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            postItems(type)

    ).flattenMerge(2).buffer()

    private fun postItems(type: ClubTabItemType, postType:PostType = getPostType(type)) = Pager(
            config = PagingConfig(pageSize = ClubItemMediator.PER_LIMIT),
            remoteMediator = ClubItemMediator(mimiDB, domainManager, adWidth, adHeight,
                    type, postType, pagingCallback)
    ) {
        mimiDB.postDBItemDao().pagingSourceByClubTab(type)
    }.flow

    private fun getPostType(type: ClubTabItemType) = when (type) {
        ClubTabItemType.FOLLOW -> PostType.FOLLOWED
        ClubTabItemType.HOTTEST -> PostType.TEXT_IMAGE_VIDEO
        ClubTabItemType.LATEST -> PostType.TEXT_IMAGE_VIDEO
        ClubTabItemType.SHORT_VIDEO -> PostType.VIDEO
        ClubTabItemType.PICTURE -> PostType.IMAGE
        ClubTabItemType.NOVEL -> PostType.TEXT
    }

    private val pagingCallback = object : PagingCallback {

        override fun onTotalCount(count: Long) {
            _postCount.postValue(count.toInt())
        }

    }
}