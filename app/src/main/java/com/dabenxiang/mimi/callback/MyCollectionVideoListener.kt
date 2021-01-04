package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.PlayItem
import com.dabenxiang.mimi.model.enums.MyCollectionTabItemType
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.VideoType

interface MyCollectionVideoListener {
    fun onMoreClick(item: PlayItem, position: Int)
    fun onLikeClick(item: PlayItem, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<PlayItem>, position: Int)
    fun onChipClick(type: VideoType, tag: String)
    fun onItemClick(item: PlayItem, type: MyCollectionTabItemType)
    fun onCommentClick(item: PlayItem, type: MyCollectionTabItemType)
    fun onFavoriteClick(
        item: PlayItem,
        position: Int,
        isFavorite: Boolean,
        type: MyCollectionTabItemType
    )
}