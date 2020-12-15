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
        const val PER_LIMIT = 10
        private const val AD_GAP: Int = 5
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
            val memberPostAdItem = MemberPostItem(type = PostType.AD, adItem = adItem)
            val list = arrayListOf<MemberPostItem>()

            memberPostItems?.forEachWithIndex { index, item ->
                if (index == 5) list.add(memberPostAdItem)
                list.add(item)
            }
            
            pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)
            database.withTransaction {
                if(loadType == LoadType.REFRESH){
                    when(type){
                        ClubTabItemType.FOLLOW ->  database.postDBItemDao().deleteItemByFollow()
                        ClubTabItemType.HOTTEST -> database.postDBItemDao().deleteItemByHottest()
                        ClubTabItemType.LATEST -> database.postDBItemDao().deleteItemByLatest()
                        else-> database.postDBItemDao().deleteItemByPostType(postType)
                    }
                    database.remoteKeyDao().deleteByType(type)
                }

                database.remoteKeyDao().insertOrReplace(RemoteKey(type, result.body()?.paging?.offset ?: 0))
                memberPostItems?.let {
                    val postDBItems = it.map {item->
                        Timber.i("ClubItemMediator postDBItems type=$type")
                        val oldItem = database.postDBItemDao().getItemById(item.id)
                        if(oldItem == null) {
                            PostDBItem(
                                    id = item.id,
                                    isFollow = type == ClubTabItemType.FOLLOW,
                                    isHottest = type == ClubTabItemType.HOTTEST,
                                    isLatest = type == ClubTabItemType.LATEST,
                                    postType =  item.type,
                                    memberPostItem = item
                            )
                        }else{
                            PostDBItem(
                                    id = oldItem.id,
                                    isFollow = if(type == ClubTabItemType.FOLLOW) true else oldItem.isFollow,
                                    isHottest = if(type == ClubTabItemType.HOTTEST) true else oldItem.isHottest,
                                    isLatest = if(type == ClubTabItemType.LATEST) true else oldItem.isLatest,
                                    postType =  item.type,
                                    memberPostItem = item
                            )
                        }

                    }

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