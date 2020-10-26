package com.dabenxiang.mimi.model.vo

import java.io.Serializable

class PlayerItem(val videoId: Long) : Serializable {

    companion object {
        fun parser(item: Any): PlayerItem {
            return when (item) {
                is BaseVideoItem.Video -> PlayerItem(
                    item.id ?: 0L
                )
                is CarouselHolderItem -> PlayerItem(
                    item.id ?: 0L
                )
                else -> PlayerItem(0L)
            }
        }
    }

}