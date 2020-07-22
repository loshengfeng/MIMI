package com.dabenxiang.mimi.model.serializable

import com.dabenxiang.mimi.model.enums.PostType
import java.io.Serializable

data class SearchPostItem(
    val type: PostType = PostType.TEXT,
    val keyword: String = ""
) : Serializable