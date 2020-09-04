package com.dabenxiang.mimi.callback

import android.widget.ImageView
import com.dabenxiang.mimi.model.enums.LoadImageType

interface AttachmentListener {
    fun onGetAttachment(id: Long?, view: ImageView, type: LoadImageType)
    fun onGetAttachment(id: String, parentPosition: Int, position: Int)
}