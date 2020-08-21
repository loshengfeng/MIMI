package com.dabenxiang.mimi.view.order

import androidx.paging.PagingSource
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.manager.DomainManager
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

class OrderPagingSource(
    private val domainManager: DomainManager
) : PagingSource<Long, OrderItem>() {
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, OrderItem> {
        Timber.d("@@load")
        return try {
            val offset = params.key ?: 0L
            Timber.d("@@load offset: $offset, loadSize: ${params.loadSize}")
            val result =
                domainManager.getApiRepository()
                    .getOrder(offset.toString(), params.loadSize.toString())
            if (!result.isSuccessful) throw HttpException(result)
            val item = result.body()
            val orders = item?.content?.orders
            val nextOffset = when {
                hasNextPage(
                    item?.paging?.count ?: 0,
                    item?.paging?.offset ?: 0,
                    orders?.size ?: 0,
                    params.loadSize
                ) -> offset + params.loadSize
                else -> null
            }

            LoadResult.Page(
                data = orders ?: arrayListOf(),
                prevKey = if (offset == 0L) null else offset - params.loadSize.toLong(),
                nextKey = nextOffset
            )
        } catch (exception: Exception) {
            Timber.e("@@@.........$exception")
            LoadResult.Error(exception)
        }
    }

    private fun hasNextPage(total: Long, offset: Long, currentSize: Int, loadSize: Int): Boolean {
        return when {
            currentSize < loadSize -> false
            offset >= total -> false
            else -> true
        }
    }
}