package com.dabenxiang.mimi.view.recommend

import com.dabenxiang.mimi.model.api.vo.DecryptSettingItem
import com.dabenxiang.mimi.model.api.vo.HomeListItem
import com.dabenxiang.mimi.model.api.vo.RecommendVideoItem

class RecommendFuncItem(
    val onItemClick: (RecommendVideoItem) -> Unit = { _ -> },
    val onMoreClick: (HomeListItem) -> Unit = { },
    val getDecryptSetting: ((String) -> DecryptSettingItem?) = { _ -> null },
    val decryptCover: (String, DecryptSettingItem, (ByteArray?) -> Unit) -> Unit = { _, _, _ -> }
)