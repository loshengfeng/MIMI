package com.dabenxiang.mimi.model.vo

import java.io.Serializable

data class ViewerItem(
        var attachmentId: String = "",
        var url: String = "",
        var isVideo: Boolean = false
) : Serializable
