package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.PostType

interface AdultListener {
    fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean)
    fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean)
    fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType)
    fun onMoreClick(item: MemberPostItem, items: List<MemberPostItem>)
    fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType)
    fun onClipItemClick(item: List<MemberPostItem>, position: Int)
    fun onClipCommentClick(item: List<MemberPostItem>, position: Int)
    fun onChipClick(type: PostType, tag: String)
    fun onAvatarClick(userId: Long, name: String)
}