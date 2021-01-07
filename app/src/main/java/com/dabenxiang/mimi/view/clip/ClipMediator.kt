package com.dabenxiang.mimi.view.clip

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ClipMediator(
    private val database: MiMiDB,
    private val domainManager: DomainManager,
    private val orderByType: StatisticsOrderType,
    private val pageCode: String
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {
    companion object {
        const val PER_LIMIT = 20
    }
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
            Timber.i("ClipMediator pageName=$pageCode offset=$offset")

            val result = domainManager.getApiRepository().searchShortVideo(
                orderByType = orderByType,
                offset = offset.toString(),
                limit = ClipPagingSource.PER_LIMIT.toString()
            )
            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()
            val videos = item?.content?.videos
            val hasNext = hasNextPage(
                item?.paging?.count ?: 0,
                item?.paging?.offset ?: 0,
                videos?.size ?: 0
            )

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }

                val nextKey = if (hasNext) offset + PER_LIMIT else null
                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                videos?.map { item ->
                    item.toMemberPostItem(PostType.SMALL_CLIP)
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

            return MediatorResult.Success(endOfPaginationReached = !hasNext)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int, loadSize: Int = PER_LIMIT): Boolean {
        return when {
            currentSize < loadSize -> false
            offset >= total -> false
            else -> true
        }
    }
}