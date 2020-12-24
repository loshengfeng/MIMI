package com.dabenxiang.mimi.model.api.vo

import java.io.Serializable

data class PostClubItem(
    val title: String,
    val request: String,
    val tags: ArrayList<String>
): Serializable