package com.dabenxiang.mimi.callback

import android.view.View
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType

interface AdultListener {
    fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean)
    fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean)
    fun onCommentClick(item: MemberPostItem)
    fun onMoreClick(item: MemberPostItem)
    fun onItemClick(item: MemberPostItem)
    fun onClipItemClick(item: List<MemberPostItem>, position: Int)
}