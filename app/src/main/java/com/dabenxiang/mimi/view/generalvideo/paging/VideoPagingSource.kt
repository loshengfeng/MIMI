package com.dabenxiang.mimi.view.generalvideo.paging

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.VideoByCategoryItem
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException

class VideoPagingSource(
    private val domainManager: DomainManager,
    private val category: String
) : PagingSource<Long, VideoByCategoryItem>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, VideoByCategoryItem> {
        return try {
            val offset = params.key ?: 0L

            val result = domainManager.getApiRepository()
                .getVideoByCategory(category, offset.toString(), params.loadSize.toString())
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val items = body?.content

            val nextOffset = when {
                hasNextPage(
                    body?.paging?.count ?: 0,
                    body?.paging?.offset ?: 0,
                    items?.size ?: 0,
                    params.loadSize
                ) -> offset + params.loadSize
                else -> null
            }

            LoadResult.Page(
                data = items ?: arrayListOf(),
                prevKey = if (offset == 0L) null else offset - params.loadSize.toLong(),
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int, loadSize: Int): Boolean {
        return when {
            currentSize < loadSize -> false
            offset >= total -> false
            else -> true
        }
    }
}