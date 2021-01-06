package com.dabenxiang.mimi.model.enums

enum class VideoType(val value: Int) {
    VIDEO_ON_DEMAND(8),
    SHORT_VIDEO(16);

    fun toPostType(): PostType {
        return when (this) {
            VIDEO_ON_DEMAND -> PostType.VIDEO_ON_DEMAND
            SHORT_VIDEO -> PostType.SMALL_CLIP
        }
    }
}