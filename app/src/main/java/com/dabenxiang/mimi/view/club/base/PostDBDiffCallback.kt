package com.dabenxiang.mimi.view.club.base

import androidx.recyclerview.widget.DiffUtil
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.db.MemberPostWithPostDBItem
import com.dabenxiang.mimi.view.club.base.PostItemAdapter.Companion.UPDATE_FAVORITE
import com.dabenxiang.mimi.view.club.base.PostItemAdapter.Companion.UPDATE_INTERACTIVE
import com.dabenxiang.mimi.view.club.base.PostItemAdapter.Companion.UPDATE_LIKE
import com.dabenxiang.mimi.view.search.video.SearchVideoAdapter

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

        override fun getChangePayload(oldItem: MemberPostItem, newItem: MemberPostItem): Any? {
            return when {
                oldItem.likeType != newItem.likeType || oldItem.likeCount != newItem.likeCount || oldItem.isFavorite != newItem.isFavorite || oldItem.favoriteCount != newItem.favoriteCount -> UPDATE_INTERACTIVE
                else -> null
            }
        }

    }
}

const val VIEW_TYPE_CLIP = 0
const val VIEW_TYPE_PICTURE = 1
const val VIEW_TYPE_TEXT = 2
const val VIEW_TYPE_DELETED = 3
const val VIEW_TYPE_AD = 4