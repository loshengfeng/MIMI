package com.dabenxiang.mimi.view.club

import com.dabenxiang.mimi.model.api.vo.MemberClubItem

class ClubFuncItem(
    val onItemClick: (MemberClubItem) -> Unit = { _ -> },
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onFollowClick: ((MemberClubItem, Boolean, ((Boolean) -> Unit)) -> Unit) = { _, _, _ -> }
)