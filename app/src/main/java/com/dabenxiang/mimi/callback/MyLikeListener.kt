package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.PostFavoriteItem

interface MyLikeListener {
    fun onMoreClick(item: PostFavoriteItem, position: Int)
    fun onLikeClick(item: PostFavoriteItem, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<PostFavoriteItem>, position: Int)
    fun onChipClick(item: PostFavoriteItem, tag: String)
    fun onItemClick(item: PostFavoriteItem, type: Int)
    fun onCommentClick(item: PostFavoriteItem, type: Int)
    fun onFavoriteClick(
        item: PostFavoriteItem,
        position: Int,
        isFavorite: Boolean,
        attachmenttype: Int
    )
}