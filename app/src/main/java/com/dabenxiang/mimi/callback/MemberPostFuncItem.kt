package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.MemberPostItem

class MemberPostFuncItem (
    val onItemClick: (MemberPostItem) -> Unit = { _ -> },
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onFollowClick: ((MemberPostItem, Boolean, ((Boolean) -> Unit)) -> Unit) = { _, _, _ -> }
)