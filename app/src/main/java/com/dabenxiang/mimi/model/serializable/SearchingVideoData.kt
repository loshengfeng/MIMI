package com.dabenxiang.mimi.model.serializable

import java.io.Serializable

class SearchingVideoData : Serializable {
    var title: String = ""
    var tag: String = ""
    var isAdult: Boolean? = null
}