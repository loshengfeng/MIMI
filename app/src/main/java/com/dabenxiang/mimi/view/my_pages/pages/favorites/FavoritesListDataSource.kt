package com.dabenxiang.mimi.view.my_pages.pages.favorites

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class FavoritesListDataSource constructor(
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val adWidth: Int,
    private val adHeight: Int,
    private val isLike: Boolean
) : PagingSource<Long, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 10
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, MemberPostItem> {
        val offset = params.key ?: 0
        return try {

            val result =
                when(isLike) {
                    false -> domainManager.getApiRepository().getPostFavorite( offset, PER_LIMIT, 7)
                    true -> domainManager.getApiRepository().getPostLike(offset, PER_LIMIT, 7)
                }
            if (!result.isSuccessful) throw HttpException(result)

            val memberPostItems =  result.body()?.content?.map {
                it.toMemberPostItem()
            } as ArrayList

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT.toLong() else null
            if (offset == 0L) pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)
            LoadResult.Page(memberPostItems ?: listOf(), null, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT.toLong() -> false
            offset >= total -> false
            else -> true
        }
    }

}