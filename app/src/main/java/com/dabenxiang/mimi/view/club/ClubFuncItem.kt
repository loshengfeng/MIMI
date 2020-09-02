package com.dabenxiang.mimi.view.club

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.MemberClubItem
import com.dabenxiang.mimi.model.enums.LoadImageType

class ClubFuncItem(
    val onItemClick: (MemberClubItem) -> Unit = { _ -> },
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFollowClick: ((MemberClubItem, Boolean, ((Boolean) -> Unit)) -> Unit) = { _, _, _ -> }
)