package com.dabenxiang.mimi

import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem
import com.dabenxiang.mimi.model.enums.AdultTabType
import com.dabenxiang.mimi.model.enums.AttachmentType
import com.dabenxiang.mimi.model.enums.PostType

interface GeneralListener {
    fun onMoreClick(item: PostFavoriteItem, position: Int)
    fun onLikeClick(item: PostFavoriteItem, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<PostFavoriteItem>, position: Int)
    fun onClipItemClick(item: List<PostFavoriteItem>, position: Int)
    fun onChipClick(type: PostType, tag: String)
    fun onItemClick(item: PostFavoriteItem, adultTabType: AdultTabType)
    fun onCommentClick(item: PostFavoriteItem, adultTabType: AdultTabType)
    fun onFavoriteClick(
        item: PostFavoriteItem,
        position: Int,
        isFavorite: Boolean,
        type: AttachmentType
    )

    fun onFollowClick(items: List<PostFavoriteItem>, position: Int, isFollow: Boolean)
    fun onAvatarClick(userId: Long, name: String)
}