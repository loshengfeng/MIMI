package com.dabenxiang.mimi.callback

import com.bumptech.glide.load.model.GlideUrl
import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class MemberPostFuncItem(
    val onItemClick: (MemberPostItem) -> Unit = { _ -> },
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onFollowClick: ((MemberPostItem, List<MemberPostItem>, Boolean, ((Boolean) -> Unit)) -> Unit) = { _,_, _, _ -> },
    val onLikeClick: ((MemberPostItem, Boolean, ((Boolean, Int) -> Unit)) -> Unit) = { _, _, _ -> },
    val onFavoriteClick: ((MemberPostItem, Boolean, ((Boolean, Int) -> Unit)) -> Unit) = { _, _, _ -> },
    val getImageResource:((String, ((GlideUrl) -> Unit)) -> Unit) = { _, _ -> }
)