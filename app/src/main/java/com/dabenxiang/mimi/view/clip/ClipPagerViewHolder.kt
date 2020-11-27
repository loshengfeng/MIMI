package com.dabenxiang.mimi.view.clip

import android.view.View
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dabenxiang.mimi.model.api.vo.VideoItem
import com.dabenxiang.mimi.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_clip_pager.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class ClipPagerViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val rvClip = itemView.rv_clip

    private val clipAdapter by lazy {
        val adapter = ClipAdapter(rvClip.context)
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
        if (rvClip.adapter == null || rvClip.tag != position) {
            rvClip.tag = position
            clipAdapter.setClipFuncItem(clipFuncItem)
            rvClip.adapter = clipAdapter
            (rvClip.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            PagerSnapHelper().attachToRecyclerView(rvClip)
            rvClip.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            val lastPos = clipAdapter.getCurrentPos()
                            val currentPos = (rvClip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                            Timber.d("SCROLL_STATE_IDLE lastPosition: $lastPos, currentPos:$currentPos")
                            takeIf { currentPos >= 0 && currentPos != lastPos }?.run {
                                clipAdapter.pausePlayer()
                                clipAdapter.releasePlayer()
                                clipAdapter.updateCurrentPosition(currentPos)
                                clipAdapter.notifyItemChanged(lastPos)
//                                clipAdapter.getVideoItem(currentPos)?.run {
//                                    clipFuncItem.getPostDetail(this, currentPos, ::updateAfterGetDeducted)
//                                }
                            } ?: clipAdapter.updateCurrentPosition(lastPos)
                        }
                    }
                }
            })
            clipFuncItem.getClips(::setupClips)
        }
    }

    private fun setupClips(data: PagingData<VideoItem>, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            (rvClip.adapter as ClipAdapter).submitData(data)
        }
    }

    private fun updateAfterGetDeducted(currentPos: Int, deducted: Boolean) {
//        clipAdapter.getMemberPostItem(currentPos)?.run { this.deducted = deducted }
//        clipAdapter.notifyItemChanged(currentPos, ClipAdapter.PAYLOAD_UPDATE_DEDUCTED)
    }
}