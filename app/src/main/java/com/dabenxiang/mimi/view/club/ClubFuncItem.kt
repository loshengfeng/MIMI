package com.dabenxiang.mimi.view.club

import android.graphics.Bitmap

class ClubFuncItem (
    val getBitmap: ((String, ((Bitmap) -> Unit)) -> Unit) = { _, _ -> }
)