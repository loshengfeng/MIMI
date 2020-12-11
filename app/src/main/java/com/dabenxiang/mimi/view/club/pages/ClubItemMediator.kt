package com.dabenxiang.mimi.view.club.pages

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.db.MiMiDB
import org.jetbrains.anko.collections.forEachWithIndex
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ClubItemMediator(
        private val database: MiMiDB,
        private val domainManager: DomainManager,
        private val adWidth: Int,
        private val adHeight: Int,
        private val type: ClubTabItemType,
        private var postType: PostType
) : RemoteMediator<Long, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 10
        private const val AD_GAP: Int = 5
    }

    override suspend fun load(
            loadType: LoadType,
            state: PagingState<Long, MemberPostItem>
    ): MediatorResult {
        try {
            val offset = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                            ?: return MediatorResult.Success(
                                    endOfPaginationReached = true
                            )

                    lastItem.id
                }
            }.takeIf { it == null }.run { 0L }


            val adItem = domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content
                    ?: AdItem()


            val result =
                    when (type) {
                        ClubTabItemType.FOLLOW -> {
                            domainManager.getApiRepository().getPostFollow(offset.toInt(), ClubItemDataSource.PER_LIMIT)
                        }
                        ClubTabItemType.RECOMMEND -> {
                            domainManager.getApiRepository().getMembersPost(
                                    PostType.TEXT_IMAGE_VIDEO,
                                    OrderBy.HOTTEST,
                                    offset.toInt(),
                                    ClubItemDataSource.PER_LIMIT
                            )
                        }
                        ClubTabItemType.LATEST -> {
                            domainManager.getApiRepository().getMembersPost(
                                    PostType.TEXT_IMAGE_VIDEO,
                                    OrderBy.NEWEST,
                                    offset.toInt(),
                                    ClubItemDataSource.PER_LIMIT
                            )
                        }
                        ClubTabItemType.SHORT_VIDEO -> {
                            domainManager.getApiRepository()
                                    .getMembersPost(PostType.VIDEO, OrderBy.NEWEST, offset.toInt(), ClubItemDataSource.PER_LIMIT)
                        }
                        ClubTabItemType.PICTURE -> {
                            domainManager.getApiRepository().getMembersPost(
                                    PostType.IMAGE, OrderBy.NEWEST,
                                    offset.toInt(), ClubItemDataSource.PER_LIMIT
                            )
                        }
                        ClubTabItemType.NOVEL -> {
                            domainManager.getApiRepository()
                                    .getMembersPost(PostType.TEXT, OrderBy.NEWEST, offset.toInt(), ClubItemDataSource.PER_LIMIT)
                        }
                    }
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content
            val memberPostAdItem = MemberPostItem(type = PostType.AD, adItem = adItem)
            val list = arrayListOf<MemberPostItem>()

            memberPostItems?.forEachWithIndex { index, item ->
                if (index == 5) list.add(memberPostAdItem)
                list.add(item)
            }

            database.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> database.memberPostDao().deleteItemByType(postType)
                    else -> {
                        memberPostItems?.let {
                            database.memberPostDao().insertAll(it)
                        }
                    }
                }
            }

            val hasNext = hasNextPage(
                    result.body()?.paging?.count ?: 0,
                    result.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )
//        val nextKey = if (hasNext) offset + ClubItemDataSource.PER_LIMIT else null

            return MediatorResult.Success(endOfPaginationReached = hasNext
            )
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int): Boolean {
        return when {
            currentSize < ClubItemDataSource.PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}