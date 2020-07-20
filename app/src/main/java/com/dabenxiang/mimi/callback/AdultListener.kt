package com.dabenxiang.mimi.callback

import android.view.View
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType

interface AdultListener {
    fun onFollowPostClick(item: MemberPostItem, position: Int, isFollow: Boolean)
    fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean)
    fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType)
    fun onMoreClick(item: MemberPostItem)
    fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType)
    fun onClipItemClick(item: List<MemberPostItem>, position: Int)
    fun onClipCommentClick(item: List<MemberPostItem>, position: Int)
}