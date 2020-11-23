package com.dabenxiang.mimi.view.recommend

import com.dabenxiang.mimi.model.api.vo.RecommendVideoItem

class RecommendFuncItem(
    val onItemClick: (RecommendVideoItem) -> Unit = { _ -> },
    val onMoreClick: () -> Unit = { }
)