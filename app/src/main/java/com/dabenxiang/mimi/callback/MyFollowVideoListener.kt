package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.MyFollowTabItemType
import com.dabenxiang.mimi.model.enums.PostType

interface MyFollowVideoListener {
    fun onMoreClick(item: PlayItem, position: Int)
    fun onLikeClick(item: PlayItem, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<PlayItem>, position: Int)
    fun onChipClick(type: PostType, tag: String)
    fun onItemClick(item: PlayItem, type: MyFollowTabItemType)
    fun onCommentClick(item: PlayItem, type: MyFollowTabItemType)
    fun onFavoriteClick(
        item: PlayItem,
        position: Int,
        isFavorite: Boolean,
        type: AttachmentType
    )
}