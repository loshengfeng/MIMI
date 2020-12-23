package com.dabenxiang.mimi.view.mypost

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.manager.DomainManager
import com.dabenxiang.mimi.view.mypost.MyPostViewModel.Companion.USER_ID_ME
import retrofit2.HttpException

class MyPostPagingSource(
    private val userId: Long,
    private val domainManager: DomainManager
) : PagingSource<Int, MemberPostItem>() {

    companion object {
        const val PER_LIMIT = 20
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MemberPostItem> {
        val offset = params.key ?: 0
        return try {
            val apiRepository = domainManager.getApiRepository()
            val result =
                if (userId == USER_ID_ME) apiRepository.getMyPost(offset = 0, limit = PER_LIMIT)
                else apiRepository.getMembersPost(
                    offset = 0,
                    limit = PER_LIMIT,
                    creatorId = userId,
                    isAdult = true
                )
            if (!result.isSuccessful) throw HttpException(result)
            val body = result.body()
            val myPostItem = body?.content

            val nextPageKey = when {
                hasNextPage(
                    body?.paging?.count ?: 0,
                    body?.paging?.offset ?: 0,
                    myPostItem?.size ?: 0
                ) -> offset + PER_LIMIT
                else -> null
            }
            LoadResult.Page(myPostItem ?: arrayListOf(), null, nextPageKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
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