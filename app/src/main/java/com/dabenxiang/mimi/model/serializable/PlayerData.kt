package com.dabenxiang.mimi.model.serializable

import com.dabenxiang.mimi.model.holder.BaseVideoItem
import java.io.Serializable

class PlayerData : Serializable {

    companion object {
        fun parser(item: Any): PlayerData {
            return when (item) {
                is BaseVideoItem.Video -> PlayerData().also {
                    it.videoId = item.id ?: 0L
                }
                else -> PlayerData()
            }
        }
    }

    var videoId = 0L

    var isAdult = false
}