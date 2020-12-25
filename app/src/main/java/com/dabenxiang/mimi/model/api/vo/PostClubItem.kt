package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import java.io.Serializable

data class PostClubItem(
    val title: String = "",
    val request: String = "",
    val tags: ArrayList<String> = arrayListOf(),
    val uploadPics: ArrayList<PostAttachmentItem> = arrayListOf(),
    val deletePics: ArrayList<String> = arrayListOf()
): Serializable