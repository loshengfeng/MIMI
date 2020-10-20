package com.dabenxiang.mimi.view.clip

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType

data class ClipFuncItem(
    val getClip: ((String, Int) -> Unit) = { _, _ -> },
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFollowClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onLikeClick: ((MemberPostItem, Int, Boolean) -> Unit) = { _, _, _ -> },
    val onCommentClick: ((MemberPostItem) -> Unit) = { _ -> },
    val onBackClick: (() -> Unit) = {},
    val getPostDetail: ((MemberPostItem, Int) -> Unit) = { _, _ -> },
    val onPlayerError: ((MemberPostItem, String) -> Unit) = { _, _ -> }
)