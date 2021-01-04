package com.dabenxiang.mimi.view.club.base

import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem

object PostDBDiffCallback{
    val diffCallback = object : DiffUtil.ItemCallback<MemberPostItem>() {
        override fun areItemsTheSame(
            oldItem: MemberPostItem,
            newItem: MemberPostItem
        ): Boolean {
            return oldItem.id== newItem.id
        }

        override fun areContentsTheSame(
                oldItem: MemberPostItem,
                newItem: MemberPostItem
        ): Boolean {
            return oldItem == newItem
        }

    }
}

const val VIEW_TYPE_CLIP = 0
const val VIEW_TYPE_PICTURE = 1
const val VIEW_TYPE_TEXT = 2
const val VIEW_TYPE_DELETED = 3
const val VIEW_TYPE_AD = 4