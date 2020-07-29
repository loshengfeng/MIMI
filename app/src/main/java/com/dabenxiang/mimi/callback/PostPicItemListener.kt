package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.vo.PostAttachmentItem

class PostPicItemListener (
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onDelete: (PostAttachmentItem) -> Unit = { _ -> },
    val onUpdate: () -> Unit = { },
    val onAddPic: () -> Unit = { }
)