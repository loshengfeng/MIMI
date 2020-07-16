package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem

interface AdultListener {
    fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean)
    fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean)
    fun onCommentClick(item: MemberPostItem)
    fun onMoreClick(item: MemberPostItem)
    fun onItemClick(item: MemberPostItem)
}