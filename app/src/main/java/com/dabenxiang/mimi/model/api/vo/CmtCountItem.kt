package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

class CmtCountItem(
    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("post")
    val post: InteractiveHistoryItem = InteractiveHistoryItem(),

    @SerializedName("parentComment")
    val parentComment: InteractiveHistoryItem = InteractiveHistoryItem(),

    @SerializedName("Comment")
    val Comment: InteractiveHistoryItem = InteractiveHistoryItem(),
)

/**
"content": {
    "id":12345678,
    "post": {
        "likeCount": 0,
        "dislikeCount": 0,
        "favoriteCount": 6,
        "followCount": 0,
        "commentCount": 10
    },
    "parentComment": {
        "likeCount": 0,
        "dislikeCount": 0,
        "commentCount": 3
    },
    "Comment": {
        "likeCount": 0,
        "dislikeCount": 0,
        "commentCount": 3
    }
}
**/