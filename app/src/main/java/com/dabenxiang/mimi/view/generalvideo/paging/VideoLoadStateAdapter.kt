package com.dabenxiang.mimi.view.generalvideo.paging

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.dabenxiang.mimi.view.generalvideo.GeneralVideoAdapter

class VideoLoadStateAdapter(private val adapter: GeneralVideoAdapter) :
    LoadStateAdapter<VideoLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: VideoLoadStateViewHolder, loadState: LoadState) {
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): VideoLoadStateViewHolder {
        return VideoLoadStateViewHolder(parent) { adapter.retry() }
    }

}