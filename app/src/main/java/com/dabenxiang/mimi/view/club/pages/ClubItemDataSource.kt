package com.dabenxiang.mimi.view.club.pages

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException

class ClubItemDataSource(
    private val domainManager: DomainManager,
    private val pagingCallback: PagingCallback,
    private val adWidth: Int,
    private val adHeight: Int,
    private val type: ClubTabItemType
) : PagingSource<Int, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 10
        private const val AD_GAP: Int = 5
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MemberPostItem> {
        val offset = params.key ?: 0
        return try {

            val adItem = domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content
                ?: AdItem()

            val result =
                when (type) {
                    ClubTabItemType.FOLLOW -> {
                        domainManager.getApiRepository().getPostFollow(offset, PER_LIMIT)
                    }
                    ClubTabItemType.RECOMMEND -> {
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
            val memberPostAdItem = MemberPostItem(type = PostType.AD, adItem = adItem)
            val list = arrayListOf<MemberPostItem>()
            memberPostItems?.forEachWithIndex { index, item ->
                if (index == 5) list.add(memberPostAdItem)
                list.add(item)
            }
            if(offset == 0) list.add(0, memberPostAdItem)
            list.add(memberPostAdItem)

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
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
}