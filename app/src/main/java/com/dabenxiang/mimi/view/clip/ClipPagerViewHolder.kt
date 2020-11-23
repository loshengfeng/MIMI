package com.dabenxiang.mimi.view.clip

import android.view.View
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import com.dabenxiang.mimi.view.order.OrderAdapter
import kotlinx.android.synthetic.main.item_clip_pager.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ClipPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val rv_clip = itemView.rv_clip

    private val clipAdapter by lazy {
        val adapter = ClipAdapter(rv_clip.context)
        val loadStateListener = { loadStatus: CombinedLoadStates ->
            when (loadStatus.refresh) {
                is LoadState.Error -> {
                    Timber.e("refresh Error:${(loadStatus.refresh as LoadState.Error).error.localizedMessage}")
                }
                is LoadState.Loading -> {
                    Timber.d("refresh Loading endOfPaginationReached:${(loadStatus.refresh as LoadState.Loading).endOfPaginationReached}")
                }
                is LoadState.NotLoading -> {
                    Timber.d("refresh NotLoading endOfPaginationReached:${(loadStatus.refresh as LoadState.NotLoading).endOfPaginationReached}")
                }
            }
            when (loadStatus.append) {
                is LoadState.Error -> {
                    Timber.e("append Error:${(loadStatus.append as LoadState.Error).error.localizedMessage}")
                }
                is LoadState.Loading -> {
                    Timber.d("append Loading endOfPaginationReached:${(loadStatus.append as LoadState.Loading).endOfPaginationReached}")
                }
                is LoadState.NotLoading -> {
                    Timber.d("append NotLoading endOfPaginationReached:${(loadStatus.append as LoadState.NotLoading).endOfPaginationReached}")
                }
            }
        }
        adapter.addLoadStateListener(loadStateListener)
        adapter
    }

    fun onBind(position: Int, clipFuncItem: ClipFuncItem) {
        if (rv_clip.adapter == null || rv_clip.tag != position) {
            rv_clip.tag = position
            clipAdapter.setClipFuncItem(clipFuncItem)
            rv_clip.adapter = clipAdapter
            (rv_clip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rv_clip)
            rv_clip.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val currentPos =
                                (rv_clip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE position: $currentPos")

                            val clipAdapter = rv_clip.adapter as ClipAdapter
                            val lastPosition = clipAdapter.getCurrentPos()
                            Timber.d("SCROLL_STATE_IDLE lastPosition: $lastPosition")
                            takeIf { currentPos >= 0 && currentPos != lastPosition }?.also {
                                clipAdapter.releasePlayer()
                                clipAdapter.updateCurrentPosition(currentPos)
                                clipAdapter.notifyItemChanged(lastPosition)
//                            clipAdapter.notifyItemChanged(currentPos)
//                            viewModel.getPostDetail(memberPostItems[currentPos], currentPos)
                            } ?: clipAdapter.updateCurrentPosition(lastPosition)
                        }
                    }
                }
            })
            clipFuncItem.getClips(::setupClips)
        }
    }

    private fun setupClips(data: PagingData<MemberPostItem>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            (rv_clip.adapter as ClipAdapter).submitData(data)
        }
    }
}