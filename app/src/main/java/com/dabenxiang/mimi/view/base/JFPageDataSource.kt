package com.dabenxiang.mimi.view.base

import com.chad.library.adapter.base.module.BaseLoadMoreModule
import java.lang.ref.WeakReference

private interface ILoadPage<Key> {
    suspend fun load(params: Key?): JFPageDataSource.JFLoadResult
}

data class JFLoadAfterResult<Value>(val isEnd: Boolean, val content: Value?)

abstract class JFPageDataSource<Key : Any, Value : Any>(loadMoreModule: BaseLoadMoreModule) :
    ILoadPage<Key> {

    private var loadInit = true
    private var nextKey: Key? = null
    private val loadMoreModule = WeakReference(loadMoreModule)

    // 重置數據
    fun resetPage() {
        loadInit = true
        nextKey = null
    }

    sealed class JFLoadResult {
        data class Page<Key : Any, Value : Any>(val nextKey: Key?, val content: List<Value>?) : JFLoadResult()
        data class PageError(val e: Exception) : JFLoadResult()
    }

    suspend fun loadMore(): JFLoadAfterResult<Value> {
        val isLoadInit = loadInit
        loadInit = false
        if (nextKey != null || isLoadInit) {
            val data = load(nextKey)
            nextKey = when (data) {
                is JFLoadResult.Page<*, *> -> {
                    if (data.nextKey == null) {
                        data.nextKey
                    } else {
                        data.nextKey as? Key
                    }
                    val result = JFLoadAfterResult(nextKey == null, data.content as? Value)
                    return setupLoadMore(result)
                }
                is JFLoadResult.PageError -> {
                    null
                }
            }
        }
        return setupLoadMore(JFLoadAfterResult(true, null))
    }

    private fun setupLoadMore(result: JFLoadAfterResult<Value>): JFLoadAfterResult<Value> {
        loadMoreModule.get()?.apply {
            if (result.isEnd) {
                loadMoreEnd()
            } else {
                loadMoreComplete()
            }
        }

        return result
    }
}