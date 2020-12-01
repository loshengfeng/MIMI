package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.enums.PostType
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

interface GeneralPostedListener : BaseItemListener {
    fun onMoreClick(item: JvmType.Object, position: Int)
    fun onLikeClick(item: JvmType.Object, position: Int, isLike: Boolean)
    fun onClipCommentClick(item: List<JvmType.Object>, position: Int)
    fun onChipClick(type: PostType, tag: String)

    fun onCommentClick(item: JvmType.Object, type: Int)
    fun onFavoriteClick(
        item: JvmType.Object,
        position: Int,
        isFavorite: Boolean,
        attachmenttype: Int
    )
}