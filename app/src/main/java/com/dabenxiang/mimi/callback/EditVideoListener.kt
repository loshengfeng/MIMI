package com.dabenxiang.mimi.callback

import android.net.Uri

interface EditVideoListener {
    fun onStart()
    fun onFinish(resourceUri: Uri)
    fun onError(errMsg: String)
}