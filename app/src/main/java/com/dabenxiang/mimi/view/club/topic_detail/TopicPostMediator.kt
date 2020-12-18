package com.dabenxiang.mimi.view.club.topic_detail

import androidx.paging.*
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.*
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.club.pages.ClubItemMediator
import org.jetbrains.anko.collections.forEachWithIndex
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
        const val TOPIC_INDEX = "991"
    }
    private val pageName= TopicPostMediator::class.simpleName + tag + orderBy.toString()

    var adItem :AdItem ? =null

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MemberPostWithPostDBItem>
    ): MediatorResult {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> {
                    adItem = null
                    database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageName, 0))
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        database.remoteKeyDao().remoteKeyByType(pageName)
                    }
                    remoteKey.offset

                }
            }.takeIf { it == null }.run { 0 }

            adItem?.takeIf { adItem?.href?.isNotEmpty() ==true }?.let { adItem } ?: run {
                adItem = domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content
                    ?: AdItem()
            }

            val result = domainManager.getApiRepository().getMembersPost(
                    offset, PER_LIMIT, tag, orderBy.value
            )
            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body()
            val memberPostItems = body?.content
            val finalItems = arrayListOf<MemberPostItem>()
            memberPostItems?.forEachWithIndex { index, item ->
                if (index==0 || index % ClubItemMediator.AD_GAP == 0) {
                    val id = (TOPIC_INDEX
                            + orderBy.value.toString()
                            + result.body()?.paging?.pageIndex.toString()
                            + index.toString()).toLong()
                    finalItems.add( MemberPostItem(id=id , type = PostType.AD, adItem = adItem))
                }
                finalItems.add(item)
            }
            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().deleteItemByClubTab(pageName)
                    database.remoteKeyDao().deleteByType(pageName)
                }

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageName, result.body()?.paging?.offset ?: 0))
                finalItems?.let {
                    val postDBItems = it.mapIndexed() { index, item ->
                        val oldItem = database.postDBItemDao().getPostDBItem(pageName, item.id)
                        if(oldItem == null) {
                            PostDBItem(
                                postDBId = item.id,
                                postType = item.type,
                                pageName= pageName,
                                timestamp = System.nanoTime()

                            )
                        }else{
                            oldItem.postDBId = item.id
                            oldItem.timestamp = System.nanoTime()
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