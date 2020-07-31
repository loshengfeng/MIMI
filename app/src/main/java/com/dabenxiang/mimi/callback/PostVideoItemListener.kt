package com.dabenxiang.mimi.callback

import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.ViewerItem

class PostVideoItemListener (
    val getBitmap: ((String, ((String) -> Unit)) -> Unit) = { _, _ -> },
    val onOpenRecorder : () -> Unit = { },
    val onDelete: (PostVideoAttachment) -> Unit = { _ -> },
    val onViewer: (ViewerItem) -> Unit = { _ -> }
)