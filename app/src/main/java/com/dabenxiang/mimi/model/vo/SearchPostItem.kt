package com.dabenxiang.mimi.model.vo

import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.model.enums.StatisticsOrderType
import java.io.Serializable

data class SearchPostItem(
    val type: PostType = PostType.TEXT,
    val orderBy: StatisticsOrderType? = null,
    val tag: String? = null,
    val keyword: String? = null
) : Serializable