package com.dabenxiang.mimi.model.vo.mqtt

import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.AttachmentType

data class FavoriteItem(
    var id: String? = null,
    var position: Int? = null,
    var memberPostItem: MemberPostItem? = null,
    val type: AttachmentType? = null
)