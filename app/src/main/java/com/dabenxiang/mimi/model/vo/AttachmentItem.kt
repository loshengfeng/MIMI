package com.dabenxiang.mimi.model.vo

import android.graphics.Bitmap
import com.dabenxiang.mimi.model.enums.AttachmentType

data class AttachmentItem(
    var id: String,
    var bitmap: Bitmap,
    var position: Int,
    val type: AttachmentType
)

data class AttachmentItem2(
    var id: String,
    var bitmap: Bitmap,
    var parentPosition: Int,
    var position: Int
)