package com.dabenxiang.mimi.view.club.pages

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.*
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
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
        private val type: ClubTabItemType,
        private val adCode:String,
        private val pagingCallback: PagingCallback,
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 5
        const val AD_GAP: Int = 5
    }
    private val pageCode = ClubItemMediator::class.simpleName+ type.toString()

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
            val memberPostItems = body?.content

            val hasNext = hasNextPage(
                    result.body()?.paging?.count ?: 0,
                    result.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )

            val adCount = ceil((memberPostItems?.size ?: 0).toFloat() / AD_GAP).toInt()
            val adItems = domainManager.getAdRepository().getAD(adCode, adWidth, adHeight, adCount)
                    .body()?.content?.get(0)?.ad?.map {
                        MemberPostItem(id= (1..2147483647).random().toLong(), type = PostType.AD, adItem = it)
                    }?: arrayListOf()
            Timber.i("adItems = $adItems ")

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
                    database.postDBItemDao().deleteItemByPageCode(adCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null

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

                adItems?.let {
                    val adDBItems = it.mapIndexed { index, item ->
                        PostDBItem(
                            postDBId = item.id,
                            postType = item.type,
                            pageCode= adCode,
                            timestamp = System.nanoTime(),
                            index = index
                        )
                    }
                    database.postDBItemDao().insertMemberPostItemAll(it)
                    database.postDBItemDao().insertAll(adDBItems)
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