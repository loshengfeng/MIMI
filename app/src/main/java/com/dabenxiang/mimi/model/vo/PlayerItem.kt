package com.dabenxiang.mimi.model.vo

import com.dabenxiang.mimi.model.holder.BaseVideoItem
import com.dabenxiang.mimi.model.holder.CarouselHolderItem
import java.io.Serializable

class PlayerItem(val videoId: Long, val isAdult: Boolean) : Serializable {

    companion object {
        fun parser(item: Any, isAdult: Boolean): PlayerItem {
            return when (item) {
                is BaseVideoItem.Video -> PlayerItem(
                    item.id ?: 0L,
                    isAdult
                )
                is CarouselHolderItem -> PlayerItem(
                    item.id ?: 0L,
                    isAdult
                )
                else -> PlayerItem(0L, false)
            }
        }
    }

}