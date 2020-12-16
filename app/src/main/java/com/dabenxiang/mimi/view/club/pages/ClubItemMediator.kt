package com.dabenxiang.mimi.view.club.pages

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.AdItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.ClubTabItemType
import com.dabenxiang.mimi.model.enums.OrderBy
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.db.RemoteKey
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
        private var postType: PostType,
        private val pagingCallback: PagingCallback,
) : RemoteMediator<Int, PostDBItem>() {

    companion object {
        const val CLUB_INDEX = "990"
        const val PER_LIMIT = 10
        const val AD_GAP: Int = 5
    }

    override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, PostDBItem>
    ): MediatorResult {
        try {
            Timber.i("ClubItemMediator loadType =$loadType  type =$type")
            val offset = when (loadType) {
                LoadType.REFRESH -> {
                    database.remoteKeyDao().insertOrReplace(RemoteKey(type, 0))
                    null
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        database.remoteKeyDao().remoteKeyByType(type)
                    }
                    remoteKey.offset

                }
            }.takeIf { it == null }.run { 0 }

            val adItem = domainManager.getAdRepository().getAD(adWidth, adHeight).body()?.content
                    ?: AdItem()

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
                                    ClubItemDataSource.PER_LIMIT
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

            val finalItems = arrayListOf<MemberPostItem>()
            Timber.i("memberPostItems paging =${result.body()?.paging}")
            memberPostItems?.forEachWithIndex { index, item ->
                Timber.i("memberPostItems index =$index")
                if (index==0 || index % AD_GAP == 0) {
                    val id = (CLUB_INDEX
                            + type.value.toString()
                            + result.body()?.paging?.pageIndex.toString()
                            + index.toString()).toLong()
                    Timber.i("memberPostItems ad index =$index  id=$id")
                    finalItems.add( MemberPostItem(id=id , type = PostType.AD, adItem = adItem))
                }
                finalItems.add(item)
            }
            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)
            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    when(type){
                        ClubTabItemType.FOLLOW,
                        ClubTabItemType.HOTTEST,
                        ClubTabItemType.LATEST,
                        ClubTabItemType.SHORT_VIDEO,
                        ClubTabItemType.PICTURE,
                        ClubTabItemType.NOVEL -> database.postDBItemDao().deleteItemByClubTab(type)
                    }
                    database.remoteKeyDao().deleteByType(type)
                }

                database.remoteKeyDao().insertOrReplace(RemoteKey(type, result.body()?.paging?.offset ?: 0))
                finalItems?.let {
                    val postDBItems = it.mapIndexed { index, item ->

                        val queryId = (CLUB_INDEX + type.value.toString() + item.id.toString().substring(3)).toLong()
                        val oldItem = database.postDBItemDao().getItemById(queryId)
                        if(oldItem == null) {
                            PostDBItem(
                                    id = queryId,
                                    postDBId = item.id,
                                    postType =  item.type,
                                    clubTabItemType= type
                            )
                        }else{
                            oldItem.postDBId = item.id
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
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }
}