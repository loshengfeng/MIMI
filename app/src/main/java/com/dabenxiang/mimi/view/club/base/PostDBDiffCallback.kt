package com.dabenxiang.mimi.view.club.base

import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem

object PostDBDiffCallback{
    val diffCallback = object : DiffUtil.ItemCallback<MemberPostWithPostDBItem>() {
        override fun areItemsTheSame(
                oldItem: MemberPostWithPostDBItem,
                newItem: MemberPostWithPostDBItem
        ): Boolean {
            return oldItem.postDBItem.id== newItem.postDBItem.id
        }

        override fun areContentsTheSame(
                oldItem: MemberPostWithPostDBItem,
                newItem: MemberPostWithPostDBItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: MemberPostWithPostDBItem, newItem: MemberPostWithPostDBItem): Any? {
            return oldItem.copy(memberPostItem = newItem.memberPostItem) == newItem
        }
    }
}

const val VIEW_TYPE_CLIP = 0
const val VIEW_TYPE_PICTURE = 1
const val VIEW_TYPE_TEXT = 2
const val VIEW_TYPE_DELETED = 3
const val VIEW_TYPE_AD = 4