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
import com.dabenxiang.mimi.view.my_pages.pages.like.MiMiLikeListDataSource
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ClubItemMediator(
        private val database: MiMiDB,
        private val domainManager: DomainManager,
        private val adWidth: Int,
        private val adHeight: Int,
        private val type: ClubTabItemType,
        private val pagingCallback: PagingCallback,
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val CLUB_INDEX = "990"
        const val PER_LIMIT = 10
        const val AD_GAP: Int = 5
    }
    private val pageName = ClubItemMediator::class.simpleName+ type.toString()

    override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, MemberPostWithPostDBItem>
    ): MediatorResult {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> {
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
            }?.toInt() ?: 0

            Timber.i("ClubItemMediator pageName=$pageName offset=$offset")

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

            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)
            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    database.postDBItemDao().deleteItemByClubTab(pageName)
                    database.remoteKeyDao().deleteByType(pageName)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null

                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageName, nextKey?.toLong()))
                val remoteKey = database.withTransaction {
                    database.remoteKeyDao().remoteKeyByType(pageName)
                }

                memberPostItems?.let {
                    val postDBItems = it.mapIndexed() { index, item ->
                        val oldItem = database.postDBItemDao().getPostDBItem(pageName, item.id)

                        when(oldItem) {
                            null ->  PostDBItem(
                                    id= item.id,
                                    postDBId = item.id,
                                    postType = item.type,
                                    pageName= pageName,
                                    timestamp = System.nanoTime(),
                                    index = index

                            )
                            else-> {
                                oldItem.postDBId = item.id
                                oldItem.timestamp = System.nanoTime()
                                oldItem.index = index
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