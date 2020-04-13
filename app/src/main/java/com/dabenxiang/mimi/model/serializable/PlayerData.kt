package com.dabenxiang.mimi.model.serializable

import com.dabenxiang.mimi.model.api.vo.StatisticsItem
import java.io.Serializable

class PlayerData : Serializable {

    companion object {
        fun parser(item: Any): PlayerData {
            return when (item) {
                is StatisticsItem -> PlayerData().also {
                    it.videoId = item.id
                }
                else -> PlayerData()
            }
        }
    }

    var videoId: Int? = -1
}