package com.dabenxiang.mimi.view.pagingfooter

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

fun <T : Any, VH : RecyclerView.ViewHolder> PagingDataAdapter<T, VH>.withMimiLoadStateFooter(
    retryListener: () -> Unit
): ConcatAdapter {
    val loadStateAdapter = PagingLoadStateAdapter(retryListener)
    return withLoadStateFooter(loadStateAdapter)
}