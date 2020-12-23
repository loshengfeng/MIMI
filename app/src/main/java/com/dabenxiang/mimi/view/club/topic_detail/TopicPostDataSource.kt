package com.dabenxiang.mimi.view.club.topic_detail

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.my_pages.pages.like.MiMiLikeListDataSource
import retrofit2.HttpException
import kotlin.math.ceil

class TopicPostDataSource(
    private val pagingCallback: PagingCallback,
    private val domainManager: DomainManager,
    private val tag: String,
    private val orderBy: OrderBy,
    private val adWidth: Int,
    private val adHeight: Int
) : PagingSource<Long, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 20
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, MemberPostItem> {
        val offset = params.key ?: 0
        return try {

            val result = domainManager.getApiRepository().getMembersPost(
                    0, PER_LIMIT, tag, orderBy.value
            )
            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body()
            val memberPostItems = body?.content

            val list = arrayListOf<MemberPostItem>()
            if (offset == 0L) {
                val topAdItem =
                    domainManager.getAdRepository().getAD("community_top", adWidth, adHeight)
                        .body()?.content?.get(0)?.ad?.first() ?: AdItem()
                list.add(MemberPostItem(type = PostType.AD, adItem = topAdItem))
            }
            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / 5).toInt()
            val adItems =
                domainManager.getAdRepository().getAD("community", adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()
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
            val nextKey = if (hasNext) offset + MiMiLikeListDataSource.PER_LIMIT_LONG else null
            if (offset == 0L) pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
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