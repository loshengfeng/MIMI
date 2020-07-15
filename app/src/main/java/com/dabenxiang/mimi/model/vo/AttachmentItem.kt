package com.dabenxiang.mimi.model.vo

import android.graphics.Bitmap
import com.dabenxiang.mimi.model.enums.AttachmentType

data class AttachmentItem(
    var id: String? = null,
    var bitmap: Bitmap? = null,
    var parentPosition: Int? = null,
    var position: Int? = null,
    val type: AttachmentType? = null
)