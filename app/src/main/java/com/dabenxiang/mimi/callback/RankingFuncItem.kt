package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.api.vo.RankingItem

class RankingFuncItem (
    val onItemClick: (RankingItem) -> Unit = { _ -> },
    val getBitmap: ((String, Int) -> Unit) = { _, _ -> }
)