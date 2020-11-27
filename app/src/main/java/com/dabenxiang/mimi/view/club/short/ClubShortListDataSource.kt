package com.dabenxiang.mimi.view.club.short

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException
import timber.log.Timber

class ClubShortListDataSource(
        private val domainManager: DomainManager,
        private val pagingCallback: PagingCallback,
        private val adWidth: Int,
        private val adHeight: Int
) : PagingSource<Int, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 10
        private const val AD_GAP: Int = 3
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MemberPostItem> {
        val offset = params.key ?: 0
        return try {

            val adItem = domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content ?: AdItem()

            val result =
                domainManager.getApiRepository().getMembersPost(PostType.VIDEO, OrderBy.NEWEST,
                    0, PER_LIMIT
                )

            Timber.i("ClubShortListDataSource result=$result")
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content

            memberPostItems?.forEachWithIndex { i, _ ->
                if (i % AD_GAP == 0)
                    memberPostItems.add(i, MemberPostItem(type = PostType.AD, adItem = adItem))
            }

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT else null
            if (offset == 0) pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
            pagingCallback.onTotalCount(body?.paging?.count ?: 0)
            LoadResult.Page(memberPostItems ?: listOf(), null, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}