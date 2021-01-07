package com.dabenxiang.mimi.view.my_pages.base

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dabenxiang.mimi.callback.PagingCallback
import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.db.DBRemoteKey
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.model.db.MiMiDB
import com.dabenxiang.mimi.model.db.PostDBItem
import com.dabenxiang.mimi.model.enums.PlayListType
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class MyPagesPostMediator(
        private val database: MiMiDB,
        private val domainManager: DomainManager,
        private val myPagesType: MyPagesType,
        private val pageCode:String,
        private val pagingCallback: PagingCallback,
) : RemoteMediator<Int, MemberPostWithPostDBItem>() {

    companion object {
        const val PER_LIMIT = 10
    }

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

            Timber.i("MyPagesPostMediator $loadType pageName=$pageCode offset=$offset")

            val result =
                    when(myPagesType) {
                        MyPagesType.LIKE_POST -> domainManager.getApiRepository().getPostLike(offset.toLong(), PER_LIMIT, 7)
                        MyPagesType.LIKE_MIMI -> domainManager.getApiRepository().getPostLike(offset.toLong(), PER_LIMIT,8 )
                        MyPagesType.FAVORITE_MIMI_VIDEO -> domainManager.getApiRepository().getPlaylist(PlayListType.FAVORITE.value,
                                true, isShortVideo = false, offset = offset.toString(), limit = PER_LIMIT.toString())
                        MyPagesType.FAVORITE_SHORT_VIDEO -> domainManager.getApiRepository().getPlaylist(PlayListType.FAVORITE.value,
                                true, isShortVideo = true, offset = offset.toString(), limit = PER_LIMIT.toString())
                        else -> domainManager.getApiRepository().getPostFavorite( offset.toLong(), PER_LIMIT, 7)
                    }
            if (!result.isSuccessful) throw HttpException(result)

            val body = result.body()
            val memberPostItems = body?.content?.map {
                when(it){
                    is PlayItem ->  it.toMemberPostItem()
                    is PostFavoriteItem -> it.toMemberPostItem()
                    else -> (it as PostFavoriteItem).toMemberPostItem()
                }
            }

            val hasNext = hasNextPage(
                    result.body()?.paging?.count ?: 0,
                    result.body()?.paging?.offset ?: 0,
                    memberPostItems?.size ?: 0
            )

            if (offset == 0 && loadType == LoadType.REFRESH) pagingCallback.onTotalCount( result.body()?.paging?.count ?: 0)

            database.withTransaction {
                if(loadType == LoadType.REFRESH){
//                    database.postDBItemDao().getPostDBIdsByPageCode(pageCode)?.forEach {id->
//                        database.postDBItemDao().getPostDBItems(id).takeIf {
//                            it.isNullOrEmpty() || it.size <=1
//                        }?.let {
//                            database.postDBItemDao().deleteMemberPostItem(id)
//                        }
//
//                    }
                    database.postDBItemDao().deleteItemByPageCode(pageCode)
                    database.remoteKeyDao().deleteByPageCode(pageCode)
                }
                val nextKey = if (hasNext) offset + PER_LIMIT else null
                Timber.i("MyPagesPostMediator $loadType nextKey=$nextKey ")
                database.remoteKeyDao().insertOrReplace(DBRemoteKey(pageCode, nextKey?.toLong()))

               memberPostItems?.map { memberPostItem ->
                    database.postDBItemDao().getMemberPostItemById(memberPostItem.id)?.videoEpisodes?.let {
                        if(it.isNotEmpty()) memberPostItem.videoEpisodes =it
                    }
                    memberPostItem
                }?.let {
                    val postDBItems = it.mapIndexed { index, item ->
                        Timber.i("MyPagesPostMediator item =${item.videoEpisodes} ")
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
            currentSize <= 0 -> false
            currentSize < PER_LIMIT -> false
            offset >= total -> false
            else -> true
        }
    }

}