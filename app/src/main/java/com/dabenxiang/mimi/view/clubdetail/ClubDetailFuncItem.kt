package com.dabenxiang.mimi.view.clubdetail

import android.widget.ImageView
import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.OrderBy

class ClubDetailFuncItem(
    val getMemberPost: (OrderBy, ((PagedList<MemberPostItem>) -> Unit)) -> Unit = { _, _ -> },
    val getBitmap: ((Long?, ImageView, LoadImageType) -> Unit) = { _, _, _ -> },
    val onFollowClick: ((MemberPostItem, List<MemberPostItem>, Boolean, ((Boolean) -> Unit)) -> Unit) = { _, _, _, _ -> },
    val onLikeClick: ((MemberPostItem, Boolean, ((Boolean, Int) -> Unit)) -> Unit) = { _, _, _ -> },
    val onCommentClick: ((MemberPostItem) -> Unit) = { _ -> }
)