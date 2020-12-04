package com.dabenxiang.mimi.view.search.video.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.search.post.paging.SearchPostAllDataSource
import retrofit2.HttpException

class SearchVideoListDataSource(
    private val domainManager: DomainManager,
    private val pagingCallback: SearchPagingCallback,
    private val category: String = "",
    private val tag: String? = null,
    private val keyword: String? = null,
    private val adWidth: Int,
    private val adHeight: Int
) : PagingSource<Long, VideoItem>() {

    companion object {
        const val PER_LIMIT = "10"
        val PER_LIMIT_LONG = PER_LIMIT.toLong()
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, VideoItem> {
        val offset = params.key ?: 0
        return try {
            val adItem =
                domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content ?: AdItem()

            val result =
                domainManager.getApiRepository().searchHomeVideos(
                    q = keyword,
                    tag = tag,
                    category = category,
                    offset = offset.toString(),
                    limit = PER_LIMIT
                )
            if (!result.isSuccessful) throw HttpException(result)
            val memberPostItems = result.body()?.content?.videos
            val list = arrayListOf<VideoItem>()
            val memberPostAdItem = VideoItem(type = PostType.AD, adItem = adItem)
            memberPostItems?.forEachIndexed { index, item ->
                if (index == 5) list.add(memberPostAdItem)
                list.add(item)
            }
            list.add(memberPostAdItem)
            adjustData(list)

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + SearchPostAllDataSource.PER_LIMIT_LONG else null
            pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
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

    private fun adjustData(list: List<VideoItem>) {
        list.forEach { videoItem ->
            videoItem.isAdult = true
            videoItem.searchingTag = tag ?: ""
            videoItem.searchingStr = keyword ?: ""
        }
    }
}