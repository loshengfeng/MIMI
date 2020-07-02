package com.dabenxiang.mimi.view.base

private interface ILoadPage<Key> {
    suspend fun load(params: Key?): JFPageDataSource.JFLoadResult
}

data class JFLoadAfterResult<Value>(val isEnd: Boolean, val content: Value?)

abstract class JFPageDataSource<Key : Any, Value : Any> :
    ILoadPage<Key> {

    private var loadInit = true
    private var nextKey: Key? = null

    // 重置數據
    fun resetPage() {
        loadInit = true
        nextKey = null
    }

    sealed class JFLoadResult {
        data class Page<Key : Any, Value : Any>(val nextKey: Key?, val content: List<Value>?) :
            JFLoadResult()

        data class PageError(val e: Exception) : JFLoadResult()
    }

    suspend fun loadMore(): JFLoadAfterResult<Value> {
        val isLoadInit = loadInit
        loadInit = false
        if (nextKey != null || isLoadInit) {
            when (val data = load(nextKey)) {
                is JFLoadResult.Page<*, *> -> {
                    if (data.nextKey != null) {
                        nextKey = data.nextKey as? Key
                    }

                    if (data.content != null) {
                        return JFLoadAfterResult(nextKey == null, data.content as? Value)
                    }
                }
            }
        }

        nextKey = null
        return JFLoadAfterResult(true, null)
    }
}