package com.dabenxiang.mimi.view.club.pages

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import kotlin.math.ceil

class ClubItemDataSource(
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val adWidth: Int,
    private val adHeight: Int,
    private val type: ClubTabItemType
) : PagingSource<Int, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 10
    }

    private var adIndex = 0
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MemberPostItem> {
        val offset = params.key ?: 0
        return try {

            val result =
                when (type) {
                    ClubTabItemType.FOLLOW -> {
                        domainManager.getApiRepository().getPostFollow(offset, PER_LIMIT)
                    }
                    ClubTabItemType.HOTTEST -> {
                        domainManager.getApiRepository().getMembersPost(
                            PostType.TEXT_IMAGE_VIDEO,
                            OrderBy.HOTTEST,
                            offset,
                            PER_LIMIT
                        )
                    }
                    ClubTabItemType.LATEST -> {
                        domainManager.getApiRepository().getMembersPost(
                            PostType.TEXT_IMAGE_VIDEO,
                            OrderBy.NEWEST,
                            offset,
                            PER_LIMIT
                        )
                    }
                    ClubTabItemType.SHORT_VIDEO -> {
                        domainManager.getApiRepository()
                            .getMembersPost(PostType.VIDEO, OrderBy.NEWEST, offset, PER_LIMIT)
                    }
                    ClubTabItemType.PICTURE -> {
                        domainManager.getApiRepository().getMembersPost(
                            PostType.IMAGE, OrderBy.NEWEST,
                            offset, PER_LIMIT
                        )
                    }
                    ClubTabItemType.NOVEL -> {
                        domainManager.getApiRepository()
                            .getMembersPost(PostType.TEXT, OrderBy.NEWEST, offset, PER_LIMIT)
                    }
                }

            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content

            val list = arrayListOf<MemberPostItem>()
            if (offset == 0) {
                val topAdItem =
                    domainManager.getAdRepository().getAD("${getAdCode()}_top", adWidth, adHeight)
                        .body()?.content?.get(0)?.ad?.first() ?: AdItem()
                list.add(MemberPostItem(type = PostType.AD, adItem = topAdItem))
            }
            adIndex = 0
            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / 5).toInt()
            val adItems =
                domainManager.getAdRepository().getAD(getAdCode(), adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()
            memberPostItems?.forEachIndexed { index, item ->
                list.add(item)
                if (index % 5 == 4) list.add(getAdItem(adItems))
            }
            if ((memberPostItems?.size ?: 0) % 5 != 0) list.add(getAdItem(adItems))

            val hasNext = hasNextPage(
                body?.paging?.count ?: 0,
                body?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT else null
            if (offset == 0) pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
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

    private fun getAdCode(): String {
        return when (type) {
            ClubTabItemType.FOLLOW -> "subscribe"
            ClubTabItemType.HOTTEST -> "recommend"
            ClubTabItemType.LATEST -> "news"
            ClubTabItemType.SHORT_VIDEO -> "video"
            ClubTabItemType.PICTURE -> "image"
            ClubTabItemType.NOVEL -> "text"
        }
    }

    private fun getAdItem(adItems: ArrayList<AdItem>): MemberPostItem {
        if (adIndex + 1 > adItems.size) adIndex = 0
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems[adIndex]
        adIndex++
        return MemberPostItem(type = PostType.AD, adItem = adItem)
    }
}