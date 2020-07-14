package com.dabenxiang.mimi.view.clip

import com.dabenxiang.mimi.model.api.vo.MemberPostItem

data class ClipFuncItem(
    val getClip: ((String, Int) -> Unit) = { _, _ -> },
    val getBitmap: ((String, Int) -> Unit) = { _, _ -> },
    val onFollowClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onLikeClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onCommentClick: ((MemberPostItem) -> Unit) = { _ -> },
    val onBackClick: (() -> Unit) = {}
)