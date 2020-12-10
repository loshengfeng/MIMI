package com.dabenxiang.mimi.model.vo

import com.dabenxiang.mimi.model.enums.VideoType
import java.io.Serializable

class SearchingVideoItem : Serializable {
    var title: String = ""
    var tag: String = ""
    var category: String = ""
    var videoType: VideoType? = null
}