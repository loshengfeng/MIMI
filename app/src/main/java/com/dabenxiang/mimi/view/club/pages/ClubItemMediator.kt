package com.dabenxiang.mimi.view.club.pages

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.*
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.google.gson.Gson
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import kotlin.math.ceil

@OptIn(ExperimentalPagingApi::class)
class ClubItemMediator(
    private val database: MiMiDB,
    private val domainManager: DomainManager,
    private val adWidth: Int,
    private val adHeight: Int,
    private val pageCode: String,
    private val type: ClubTabItemType,
    private val adCode: String,
    private val pagingCallback: PagingCallback,
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 5
        const val AD_GAP: Int = 5
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

            Timber.i("ClubItemMediator pageName=$pageCode offset=$offset")

            val result =
                when (type) {
                    ClubTabItemType.FOLLOW -> {
                        domainManager.getApiRepository().getPostFollow(offset, PER_LIMIT)
                    }
                    ClubTabItemType.HOTTEST -> {
                        domainManager.getApiRepository().getMembersPost(
                            PostType.TEXT_IMAGE_VIDEO,
                            OrderBy.HOTTEST,
                            offset,
                            PER_LIMIT
                        )
                    }
                    ClubTabItemType.LATEST -> {
                        domainManager.getApiRepository().getMembersPost(
                            PostType.TEXT_IMAGE_VIDEO,
                            OrderBy.NEWEST,
                            offset,
                            PER_LIMIT
                        )
                    }
                    ClubTabItemType.SHORT_VIDEO -> {
                        domainManager.getApiRepository()
                            .getMembersPost(PostType.VIDEO, OrderBy.NEWEST, offset, PER_LIMIT)
                    }
                    ClubTabItemType.PICTURE -> {
                        domainManager.getApiRepository().getMembersPost(
                            PostType.IMAGE, OrderBy.NEWEST,
                            offset, PER_LIMIT
                        )
                    }
                    ClubTabItemType.NOVEL -> {
                        domainManager.getApiRepository()
                            .getMembersPost(PostType.TEXT, OrderBy.NEWEST, offset, PER_LIMIT)
                    }
                }

            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostApiItems = body?.content

            val hasNext = hasNextPage(
                result.body()?.paging?.count ?: 0,
                result.body()?.paging?.offset ?: 0,
                memberPostApiItems?.size ?: 0
            )

            val adCount = ceil((memberPostApiItems?.size ?: 0).toFloat() / AD_GAP).toInt()
            val adItems = domainManager.getAdRepository().getAD(adCode, adWidth, adHeight, adCount)
                .body()?.content?.get(0)?.ad ?: arrayListOf()
            Timber.i("adItems = $adItems ")

            pagingCallback.onTotalCount(result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
//                    database.postDBItemDao().getPostDBIdsByPageCode(pageCode)?.forEach {id->
//                        database.postDBItemDao().getPostDBItems(id).takeIf {
//                            it.isNullOrEmpty() || it.size <=1
//                        }?.let {
//                            database.postDBItemDao().deleteMemberPostItem(id)
//                        }
//
//                    }
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.postDBItemDao().deleteItemByPageCode(adCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

                memberPostApiItems?.forEachIndexed { index, item ->
                    if(type == ClubTabItemType.FOLLOW ) item.deducted = true
                    val oldItem = database.postDBItemDao().getPostDBItem(pageCode, item.id)

                    val postItem = when (oldItem) {
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

                    if (index % 5 == 4) item.adItem = getAdItem(adItems) else item.adItem = null

                    database.postDBItemDao().insertMemberPostItem(item)
                    database.postDBItemDao().insertItem(postItem)
                }
//                database.postDBItemDao().insertMemberPostItemAll(memberPostItems)
//                database.postDBItemDao().insertAll(postDBItems)

//                adItems.let {
//                    val adDBItems = it.mapIndexed { index, item ->
//                        PostDBItem(
//                            postDBId = item.id,
//                            postType = item.type,
//                            pageCode= adCode,
//                            timestamp = System.nanoTime(),
//                            index = index
//                        )
//                    }
//                    database.postDBItemDao().insertMemberPostItemAll(it)
//                    database.postDBItemDao().insertAll(adDBItems)
//                }
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

    private fun getAdItem(adItems: ArrayList<AdItem>): AdItem {
        if (adIndex + 1 > adItems.size) adIndex = 0
        val adItem =
            if (adItems.isEmpty()) AdItem()
            else adItems[adIndex]
        adIndex++
        return adItem
    }
}