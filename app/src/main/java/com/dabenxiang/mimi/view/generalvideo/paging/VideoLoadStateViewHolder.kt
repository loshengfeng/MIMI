package com.dabenxiang.mimi.view.generalvideo.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.item_network_state.view.*

class VideoLoadStateViewHolder(
    parent: ViewGroup,
    private val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_network_state, parent, false)
) {
    private val progressBar = itemView.progress_bar
    private val errorMsg = itemView.error_msg
    private val retry = itemView.retry_button.also { it.setOnClickListener { retryCallback() } }

    fun bindTo(loadState: LoadState) {
        progressBar.visibility = takeIf { loadState is LoadState.Loading }?.let { View.VISIBLE }
            ?: let { View.INVISIBLE }

        retry.visibility =
            takeIf { loadState is LoadState.Error }?.let { View.VISIBLE } ?: let { View.INVISIBLE }

        errorMsg.visibility =
            takeIf { !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank() }?.let { View.VISIBLE }
                ?: let { View.INVISIBLE }

        errorMsg.text = (loadState as? LoadState.Error)?.error?.message
    }

}