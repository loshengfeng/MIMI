package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.enums.AttachmentType

interface AttachmentListener {
    fun onGetAttachment(id: String, position: Int, type: AttachmentType)
    fun onGetAttachment(id: String, parentPosition: Int, position: Int)
}