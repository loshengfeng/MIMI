package com.dabenxiang.mimi.model.serializable

import com.dabenxiang.mimi.model.enums.PostType
import java.io.Serializable

data class SearchPostItem(
    val type: PostType = PostType.TEXT,
    val tag: String = "",
    val isPostFollow: Boolean = false,
    val searchText: String = ""
) : Serializable