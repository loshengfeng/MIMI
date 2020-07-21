package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.enums.PostType

data class MoreDialogData(
        var id: Long,
        var reported: Boolean = false,
        val type: PostType = PostType.TEXT
)