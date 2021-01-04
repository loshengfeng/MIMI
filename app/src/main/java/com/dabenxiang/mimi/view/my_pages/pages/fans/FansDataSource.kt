package com.dabenxiang.mimi.view.my_pages.pages.fans

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.FansItem
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class FansDataSource constructor(
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback
) : PagingSource<Long, FansItem>() {

    companion object {
        const val PER_LIMIT = 10
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, FansItem> {
        val offset = params.key ?: 0
        return try {
            val result =
                domainManager.getApiRepository().getMyFans(offset.toInt(), PER_LIMIT)
            if (!result.isSuccessful) throw HttpException(result)
            val items = result.body()?.content
            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                items?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT_LONG else null
            if (offset == 0L) pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
//            val idList = ArrayList<Long>()
//            items?.forEach {
//                idList.add(it.userId)
//            }
//            pagingCallback.onIdList(idList, false)
            LoadResult.Page(items ?: listOf(), null, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT_LONG -> false
            offset >= total -> false
            else -> true
        }
    }
}