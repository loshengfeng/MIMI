package com.dabenxiang.mimi.view.club.topic_detail

import androidx.paging.*
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.*
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.club.pages.ClubItemMediator
import com.dabenxiang.mimi.view.club.topic_detail.TopicListFragment.Companion.AD_CODE
import com.dabenxiang.mimi.view.club.topic_detail.TopicListFragment.Companion.AD_GAP
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import kotlin.math.ceil

@OptIn(ExperimentalPagingApi::class)
class TopicPostMediator(
    private val database: MiMiDB,
    private val pagingCallback: PagingCallback,
    private val domainManager: DomainManager,
    private val pageCode: String,
    private val tag: String,
    private val orderBy: OrderBy,
    private val adWidth: Int,
    private val adHeight: Int
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

            Timber.w("TopicPostMediator pageCode=$pageCode offset=$offset tag=$tag orderBy=$orderBy")
            val result = domainManager.getApiRepository().getMembersPost(
                    offset, PER_LIMIT, tag, orderBy.value
            )
            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body()
            val memberPostItems = body?.content

            Timber.w("TopicPostMediator pageCode=$pageCode memberPostItems=$memberPostItems ")
            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )

            Timber.w("hasNext =$hasNext")
            val nextKey = if (hasNext) offset + ClubItemMediator.PER_LIMIT else null

            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / AD_GAP).toInt()
            val adItems = domainManager.getAdRepository().getAD(AD_CODE, adWidth, adHeight, adCount)
                .body()?.content?.get(0)?.ad?.map {
                    MemberPostItem(id= (1..2147483647).random().toLong(), type = PostType.AD, adItem = it)
                }?: arrayListOf()

            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.postDBItemDao().deleteItemByPageCode(AD_CODE)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                memberPostItems?.let {
                    val postDBItems = it.mapIndexed { index, item ->
                        val oldItem = database.postDBItemDao().getPostDBItem(pageCode, item.id)
                        when(oldItem) {
                            null->  PostDBItem(
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

            adItems?.let {
                val adDBItems = it.mapIndexed { index, item ->
                    PostDBItem(
                        postDBId = item.id,
                        postType = item.type,
                        pageCode= AD_CODE,
                        timestamp = System.nanoTime(),
                        index = index
                    )
                }
                database.postDBItemDao().insertMemberPostItemAll(it)
                database.postDBItemDao().insertAll(adDBItems)
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