package com.dabenxiang.mimi.model.serializable

import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import java.io.Serializable

class PlayerData(val videoId: Long, val isAdult: Boolean) : Serializable {

    companion object {
        fun parser(item: Any, isAdult: Boolean): PlayerData {
            return when (item) {
                is BaseVideoItem.Video -> PlayerData(item.id ?: 0L, isAdult)
                is CarouselHolderItem -> PlayerData(item.id ?: 0L, isAdult)
                else -> PlayerData(0L, false)
            }
        }
    }


}