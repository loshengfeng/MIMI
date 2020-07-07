package com.dabenxiang.mimi.model.vo

import android.graphics.Bitmap
import com.dabenxiang.mimi.model.enums.HomeItemType

data class AttachmentItem(
    var id: Long,
    var bitmap: Bitmap,
    var position: Int,
    val type: HomeItemType
)
