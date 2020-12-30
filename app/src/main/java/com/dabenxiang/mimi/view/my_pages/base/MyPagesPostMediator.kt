package com.dabenxiang.mimi.view.my_pages.base

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class MyPagesPostMediator(
        private val database: MiMiDB,
        private val domainManager: DomainManager,
        private val myPagesType: MyPagesType,
        private val pagingCallback: PagingCallback,
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 10
    }
    private val pageCode = MyPagesPostMediator::class.simpleName + myPagesType.toString()

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

            Timber.i("MyPagesPostMediator pageName=$pageCode offset=$offset")

            val result =
                    when(myPagesType) {
                        MyPagesType.LIKE -> domainManager.getApiRepository().getPostLike(offset.toLong(), PER_LIMIT, 7)
                        MyPagesType.LIKE_MIMI -> domainManager.getApiRepository().getPostLike(offset.toLong(), PER_LIMIT,8 )
                        else -> domainManager.getApiRepository().getPostFavorite( offset.toLong(), PER_LIMIT, 7)
                    }
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content?.map {
                it.toMemberPostItem()
            }

            val hasNext = hasNextPage(
                    result.body()?.paging?.count ?: 0,
                    result.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )

            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().getPostDBIdsByPageCode(pageCode)?.forEach {id->
                        database.postDBItemDao().getPostDBItems(id).takeIf {
                            it.isNullOrEmpty() || it.size <=1
                        }?.let {
                            database.postDBItemDao().deleteMemberPostItem(id)
                        }

                    }
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                memberPostItems?.let {
                    val postDBItems = it.mapIndexed { index, item ->
                        val oldItem = database.postDBItemDao().getPostDBItem(pageCode, item.id)

                        when(oldItem) {
                            null-> PostDBItem(
                                    postDBId = item.id,
                                    postType = item.type,
                                    pageCode= pageCode,
                                    timestamp = System.nanoTime(),
                                    index = offset+index

                            )
                            else-> {
                                oldItem.postDBId = item.id
                                oldItem.timestamp = System.nanoTime()
                                oldItem.index = offset+index
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

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

}