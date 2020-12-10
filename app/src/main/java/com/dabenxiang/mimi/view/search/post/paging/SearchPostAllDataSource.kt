package com.dabenxiang.mimi.view.search.post.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class SearchPostAllDataSource constructor(
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val type: PostType,
    private val keyword: String?,
    private val tag: String?,
    private val orderBy: StatisticsOrderType,
    private val adWidth: Int,
    private val adHeight: Int
) : PagingSource<Long, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, MemberPostItem> {
        val offset = params.key ?: 0
        return try {

            val adItem =
                domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content ?: AdItem()

            val result =
                domainManager.getApiRepository()
                    .searchPostAll(type, keyword, tag, orderBy, offset.toInt(), PER_LIMIT.toInt())
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content
            val list = arrayListOf<MemberPostItem>()
            val memberPostAdItem = MemberPostItem(type = PostType.AD, adItem = adItem)
            memberPostItems?.forEachIndexed { index, item ->
                list.add(item)
                if (index % 5 == 4) list.add(memberPostAdItem)
            }
            if ((memberPostItems?.size ?: 0) % 5 != 0) list.add(memberPostAdItem)

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT_LONG else null
            pagingCallback.onTotalCount(body?.paging?.count ?: 0)
            LoadResult.Page(list, null, nextKey)
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