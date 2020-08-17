package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.ViewerItem

class PostPicItemListener (
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onDelete: (PostAttachmentItem) -> Unit = { _ -> },
    val onUpdate: () -> Unit = { },
    val onAddPic: () -> Unit = { },
    val onViewer: (ViewerItem) -> Unit = { _ -> }
)