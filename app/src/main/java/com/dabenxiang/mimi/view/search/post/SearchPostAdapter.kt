package com.dabenxiang.mimi.view.search.post

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.adapter.MemberPostPagedAdapter

class SearchPostAdapter(val context: Context) :
    PagedListAdapter<MemberPostItem, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        const val PAYLOAD_UPDATE_LIKE_AND_FOLLOW_UI = 0
        const val VIEW_TYPE_CLIP = 0
        const val VIEW_TYPE_PICTURE = 1
        const val VIEW_TYPE_TEXT = 2

        private val diffCallback = object : DiffUtil.ItemCallback<MemberPostItem>() {
            override fun areItemsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean = oldItem == newItem

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
            ): Boolean = oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

}