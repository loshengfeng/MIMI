package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem

class RankingFuncItem (
    val onItemClick: (MutableList<PostStatisticsItem>, Int) -> Unit = { _, _ -> },
    val onVideoItemClick: (StatisticsItem) -> Unit = { _ -> },
    val getBitmap: ((String, Int) -> Unit) = { _, _ -> }
)