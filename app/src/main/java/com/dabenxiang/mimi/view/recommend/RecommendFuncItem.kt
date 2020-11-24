package com.dabenxiang.mimi.view.recommend

import com.dabenxiang.mimi.model.api.vo.RecommendVideoItem
import com.dabenxiang.mimi.model.api.vo.ThirdMenuItem

class RecommendFuncItem(
    val onItemClick: (RecommendVideoItem) -> Unit = { _ -> },
    val onMoreClick: (ThirdMenuItem) -> Unit = { }
)