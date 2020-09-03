package com.dabenxiang.mimi.callback

import android.widget.ImageView

interface PostAttachmentListener {
    fun getAttachment(id: Long?,view: ImageView)
}