package com.dabenxiang.mimi.model.api.vo

sealed class DownloadResult {
    data class Success(val url: String) : DownloadResult()

    data class Error(val message: String, val cause: Any? = null) : DownloadResult()

    data class Progress(val progress: Int): DownloadResult()

    data class Redirect(val url: String): DownloadResult()
}
