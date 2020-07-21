package com.dabenxiang.mimi.view.home.club

import androidx.paging.PageKeyedDataSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.manager.DomainManager
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.PostType
import kotlinx.coroutines.CoroutineScope

class ClubPostDataSource (
    private val pagingCallback: PagingCallback,
    private val viewModelScope: CoroutineScope,
    private val domainManager: DomainManager,
    private val postType: PostType
) : PageKeyedDataSource<Int, MemberClubItem>() {

    companion object {
        const val PER_LIMIT = 20
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, MemberClubItem>
    ) {
        TODO("Not yet implemented")
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, MemberClubItem>) {
        TODO("Not yet implemented")
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, MemberClubItem>) {
        TODO("Not yet implemented")
    }

}