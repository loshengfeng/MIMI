package com.dabenxiang.mimi.view.search.post.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import kotlin.math.ceil
import kotlin.math.round

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
        const val PER_LIMIT = 10
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, MemberPostItem> {
        val offset = params.key ?: 0L
        return try {
            val result =
                domainManager.getApiRepository()
                    .searchPostAll(type, keyword, tag, orderBy, offset.toInt(), PER_LIMIT)
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content

            val topAdItem =
                domainManager.getAdRepository().getAD("search_top", adWidth, adHeight)
                    .body()?.content?.get(0)?.ad?.first() ?: AdItem()
            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / 5).toInt()
            val adItems =
                domainManager.getAdRepository().getAD("search", adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()
            val list = arrayListOf<MemberPostItem>()
            if(offset == 0L) list.add(MemberPostItem(type = PostType.AD, adItem = topAdItem))
            memberPostItems?.forEachIndexed { index, item ->
                list.add(item)
                if (index % 5 == 4) list.add(getAdItem(adItems))
            }
            if ((memberPostItems?.size ?: 0) % 5 != 0) list.add(getAdItem(adItems))

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT else null
            pagingCallback.onTotalCount(body?.paging?.count ?: 0)
            LoadResult.Page(list, null, nextKey)
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

    private fun getAdItem(adItems: ArrayList<AdItem>): MemberPostItem {
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems.removeFirst()
        return MemberPostItem(type = PostType.AD, adItem = adItem)
    }
}