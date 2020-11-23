package com.dabenxiang.mimi.view.recommend

import android.widget.ImageView
import com.dabenxiang.mimi.model.api.vo.CategoryBanner

class BannerFuncItem(
    val onItemClick: (CategoryBanner) -> Unit = { _ -> },
    val getBitmap: (Long, ImageView) -> Unit = { _, _ -> }
)