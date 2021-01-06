package com.dabenxiang.mimi.view.search.video

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.SearchPagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import kotlin.math.ceil

@OptIn(ExperimentalPagingApi::class)
class SearchVideoMediator(
    private val database: MiMiDB,
    private val domainManager: DomainManager,
    private val pagingCallback: SearchPagingCallback,
    private val pageCode: String,
    private val category: String = "",
    private val tag: String? = null,
    private val keyword: String? = null,
    private val adWidth: Int,
    private val adHeight: Int,
    private val videoType: VideoType
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 10
        const val AD_GAP = 5
    }

    private var adIndex = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MemberPostWithPostDBItem>
    ): MediatorResult {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> {
                    database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, null))
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        database.remoteKeyDao().remoteKeyByPageCode(pageCode)
                    }

                    if (remoteKey?.offset == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKey.offset
                }
            }?.toInt() ?: 0

            val result = domainManager.getApiRepository().searchHomeVideos(
                q = keyword,
                tag = tag,
                category = category,
                type = videoType,
                offset = offset.toString(),
                limit = PER_LIMIT.toString()
            )

            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostApiItems = body?.content?.videos

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostApiItems?.size ?: 0
            )
            val adCount =
                ceil((memberPostApiItems?.size ?: 0).toFloat() / AD_GAP).toInt()
            val adItems =
                domainManager.getAdRepository().getAD("search", adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()

            if (loadType == LoadType.REFRESH) {
                pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                memberPostApiItems?.map { item ->
                    item.adItem = getAdItem(adItems)
                    item.toMemberPostItem(videoType?.toPostType() ?: PostType.VIDEO_ON_DEMAND)
                }?.let {
                    val postDBItems = it.mapIndexed { index, item ->
                        when (val oldItem =
                            database.postDBItemDao().getPostDBItem(pageCode, item.id)) {
                            null -> PostDBItem(
                                postDBId = item.id,
                                postType = item.type,
                                pageCode = pageCode,
                                timestamp = System.nanoTime(),
                                index = offset + index
                            )
                            else -> {
                                oldItem.timestamp = System.nanoTime()
                                oldItem.index = offset + index
                                oldItem
                            }
                        }
                    }
                    database.postDBItemDao().insertMemberPostItemAll(it)
                    database.postDBItemDao().insertAll(postDBItems)
                }
            }
            if (!hasNext && (result.body()?.paging?.count ?: 0) % 5 != 0L) pagingCallback.onLoaded()
            return MediatorResult.Success(endOfPaginationReached = !hasNext)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

    private fun getAdItem(adItems: ArrayList<AdItem>): AdItem {
        if (adIndex + 1 > adItems.size) adIndex = 0
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems[adIndex]
        adIndex++
        return adItem
    }
}