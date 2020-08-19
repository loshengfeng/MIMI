package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType

interface MyPostListener {
    fun onMoreClick(item: MemberPostItem)
    fun onLikeClick(item: MemberPostItem, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<MemberPostItem>, position: Int)
    fun onClipItemClick(item: List<MemberPostItem>, position: Int)
    fun onChipClick(type: PostType, tag: String)
    fun onItemClick(item: MemberPostItem, adultTabType: AdultTabType)
    fun onCommentClick(item: MemberPostItem, adultTabType: AdultTabType)
    fun onFavoriteClick(
        item: MemberPostItem,
        position: Int,
        isFavorite: Boolean,
        type: AttachmentType
    )

    fun onFollowClick(items: List<MemberPostItem>, position: Int, isFollow: Boolean)
}