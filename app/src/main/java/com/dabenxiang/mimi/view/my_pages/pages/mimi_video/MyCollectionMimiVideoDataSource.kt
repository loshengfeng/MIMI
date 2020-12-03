package com.dabenxiang.mimi.view.my_pages.pages.mimi_video

import androidx.paging.PagingSource
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.*
import com.dabenxiang.mimi.model.manager.DomainManager
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException
import timber.log.Timber

class MyCollectionMimiVideoDataSource(
        private val domainManager: DomainManager,
        private val pagingCallback: PagingCallback,
        private val adWidth: Int,
        private val adHeight: Int,
        private val type: MyCollectionTabItemType
) : PagingSource<Int, PlayItem>() {

    companion object {
        const val PER_LIMIT = 10
        private const val AD_GAP: Int = 3
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PlayItem> {
        val offset = params.key ?: 0
        return try {
            val result =
                    when (type) {
                        MyCollectionTabItemType.MIMI_VIDEO -> {
                            domainManager.getApiRepository().getPlaylist(PlayListType.FAVORITE.value, true, isShortVideo = false, offset = offset.toString(), limit = PER_LIMIT.toString())
                        }
                        MyCollectionTabItemType.SHORT_VIDEO -> {
                            domainManager.getApiRepository().getPlaylist(PlayListType.FAVORITE.value, true, isShortVideo = true, offset = offset.toString(), limit = PER_LIMIT.toString())
                        }
                        else -> null
                    }
            if (result?.isSuccessful == false) throw HttpException(result)

            val body = result?.body()
            val memberPostItems = body?.content

            val hasNext = hasNextPage(
                    result?.body()?.paging?.count ?: 0,
                    result?.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )
            val nextKey = if (hasNext) offset + PER_LIMIT else null
            if (offset == 0) pagingCallback.onTotalCount(result?.body()?.paging?.count ?: 0)
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