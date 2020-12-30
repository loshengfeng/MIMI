package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.VideoType
import com.dabenxiang.mimi.view.my_pages.base.MyPagesType

interface MyCollectionVideoListener {
    fun onMoreClick(item: PlayItem, position: Int)
    fun onLikeClick(item: PlayItem, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<PlayItem>, position: Int)
    fun onChipClick(type: VideoType, tag: String)
    fun onItemClick(item: PlayItem, type: MyPagesType)
    fun onCommentClick(item: PlayItem, type: MyPagesType)
    fun onFavoriteClick(
        item: PlayItem,
        position: Int,
        isFavorite: Boolean,
        type: MyPagesType
    )
}