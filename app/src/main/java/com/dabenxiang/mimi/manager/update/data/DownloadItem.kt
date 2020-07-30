package com.dabenxiang.mimi.manager.update.data

data class DownloadItem(
    var currentSize: Int = -1,
    var totalSize: Int = -1,
    var status: Int = 0,
    var title: String = ""
)