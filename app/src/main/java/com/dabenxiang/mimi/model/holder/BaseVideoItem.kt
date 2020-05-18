package com.dabenxiang.mimi.model.holder


sealed class BaseVideoItem {
    data class Banner(val imgUrl: String?) : BaseVideoItem()
    data class Video(
        val title: String?,
        val resolution: String?,
        val info: String?,
        val imgUrl: String?,
        val isAdult: Boolean
    ) : BaseVideoItem()
}

