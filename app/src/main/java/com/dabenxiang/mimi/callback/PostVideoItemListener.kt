package com.dabenxiang.mimi.callback

import android.widget.ImageView
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import com.dabenxiang.mimi.model.vo.ViewerItem

class PostVideoItemListener (
    val getBitmap: ((Long?, ImageView) -> Unit) = { _, _ -> },
    val onOpenRecorder : () -> Unit = { },
    val onDelete: (PostVideoAttachment) -> Unit = { _ -> },
    val onViewer: (ViewerItem) -> Unit = { _ -> }
)