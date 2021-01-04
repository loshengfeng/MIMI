package com.dabenxiang.mimi.callback

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.DecryptSettingItem
import com.dabenxiang.mimi.model.api.vo.PostStatisticsItem
import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import com.dabenxiang.mimi.model.api.vo.VideoItem

class RankingFuncItem (
    val onItemClick: (MutableList<PostStatisticsItem>, Int) -> Unit = { _, _ -> },
    val onClipItemClick: (VideoItem) -> Unit = { _ -> },
    val onVideoItemClick: (StatisticsItem) -> Unit = { _ -> },
    val getBitmap: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val getDecryptSetting: ((String) -> DecryptSettingItem?) = { _ -> null },
    val decryptCover: (String, DecryptSettingItem, (ByteArray?) -> Unit) -> Unit = { _, _, _ -> }
)