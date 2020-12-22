package com.dabenxiang.mimi.view.club.topic_detail

import androidx.paging.*
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.db.*
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.club.pages.ClubItemMediator
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TopicPostMediator(
    private val database: MiMiDB,
    private val pagingCallback: PagingCallback,
    private val domainManager: DomainManager,
    private val tag: String,
    private val orderBy: OrderBy,
    private val adWidth: Int,
    private val adHeight: Int
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 20
    }
    private val pageCode= TopicPostMediator::class.simpleName + tag + orderBy.toString()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MemberPostWithPostDBItem>
    ): MediatorResult {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> {
                    database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, 0))
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        database.remoteKeyDao().remoteKeyByPageCode(pageCode)
                    }
                    remoteKey.offset

                }
            }?.toInt() ?: 0

            val result = domainManager.getApiRepository().getMembersPost(
                    offset, PER_LIMIT, tag, orderBy.value
            )
            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body()
            val memberPostItems = body?.content

            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode,
                    result.body()?.paging?.offset ?: 0 + ClubItemMediator.PER_LIMIT.toLong()))
                memberPostItems?.let {
                    val postDBItems = it.mapIndexed() { index, item ->
                        val oldItem = database.postDBItemDao().getPostDBItem(pageCode, item.id)
                        if(oldItem == null) {
                            PostDBItem(
                                postDBId = item.id,
                                postType = item.type,
                                pageCode= pageCode,
                                timestamp = System.nanoTime(),
                                index =index
                            )
                        }else{
                            oldItem.postDBId = item.id
                            oldItem.timestamp = System.nanoTime()
                            oldItem.index =index
                            oldItem
                        }


                    }
                    database.postDBItemDao().insertMemberPostItemAll(it)
                    database.postDBItemDao().insertAll(postDBItems)
                }

            }

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )
            return MediatorResult.Success(endOfPaginationReached = hasNext)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < ClubItemMediator.PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}