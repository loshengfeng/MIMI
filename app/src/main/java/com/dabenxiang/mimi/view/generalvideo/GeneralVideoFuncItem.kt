package com.dabenxiang.mimi.view.generalvideo

import com.dabenxiang.mimi.model.api.vo.DecryptSettingItem

data class GeneralVideoFuncItem(
    val getDecryptSetting: ((String) -> DecryptSettingItem?) = { _ -> null },
    val decryptCover: (String, DecryptSettingItem, (ByteArray?) -> Unit) -> Unit = { _, _, _ -> }
)