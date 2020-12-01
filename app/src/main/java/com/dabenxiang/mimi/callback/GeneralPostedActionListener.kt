package com.dabenxiang.mimi.callback

interface GeneralPostedActionListener : BaseItemListener {
    fun onLikeClick(item: Any, position: Int, isLike: Boolean)
    fun onFollowClick(item: Any, position: Int, isLike: Boolean)
    fun onFavoriteClick(
        item: Any,
        position: Int,
        isFavorite: Boolean,
        attachmenttype: Int
    )
}