package com.dabenxiang.mimi.model.api.vo

import com.google.gson.annotations.SerializedName

data class ChatContent(
        @SerializedName("withAgentFriendlyName")
        val withAgentFriendlyName: String? = ""
): BaseChatContentItem()
