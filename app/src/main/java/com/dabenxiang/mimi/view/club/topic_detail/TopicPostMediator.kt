package com.dabenxiang.mimi.view.club.topic_detail

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
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
    private val adCode: String,
    private val tag: String,
    private val orderBy: OrderBy,
    private val adWidth: Int,
    private val adHeight: Int
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 5
    }

    private var adIndex = 0

    val apiRepository = domainManager.getApiRepository()
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

            val result = apiRepository.getMembersPost(
                    offset, PER_LIMIT, tag, orderBy.value
            )

            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body()
            val memberPostItems = body?.content

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostItems?.size ?: 0
            )

            val nextKey = if (hasNext) offset + PER_LIMIT else null

            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / AD_GAP).toInt()
            val adItems = domainManager.getAdRepository().getAD(adCode, adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad ?: arrayListOf()

            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.postDBItemDao().deleteItemByPageCode(AD_CODE)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                memberPostItems?.map { item->
                    item.deducted = true
                    item.adItem = getAdItem(adItems)
                    item
                }?.let {
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
            if (!hasNext && (result.body()?.paging?.count ?: 0) % 5 != 0L) pagingCallback.onLoaded()
            return MediatorResult.Success(endOfPaginationReached = !hasNext)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            Timber.i("TopicPostMediator pageCode=$pageCode HttpException =$e")
            return MediatorResult.Error(e)
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

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}