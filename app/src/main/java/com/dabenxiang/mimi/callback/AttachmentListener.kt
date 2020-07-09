package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.enums.HomeItemType

interface AttachmentListener {
    fun onGetAttachment(id: Long, position: Int, type: HomeItemType)
    fun onGetAttachment(id: Long, position: Int)
}