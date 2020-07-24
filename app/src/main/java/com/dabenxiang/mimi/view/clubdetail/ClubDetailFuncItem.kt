package com.dabenxiang.mimi.view.clubdetail

import androidx.paging.PagedList
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.OrderBy

class ClubDetailFuncItem(
    val getMemberPost: (OrderBy, ((PagedList<MemberPostItem>) -> Unit)) -> Unit = { _, _ -> },
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> }
)