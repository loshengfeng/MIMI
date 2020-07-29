package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.PostVideoAttachment

class PostVideoItemListener (
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onOpenRecorder : () -> Unit = { },
    val onDelete: (PostVideoAttachment) -> Unit = { _ -> }
)