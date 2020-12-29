package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.vo.PostAttachmentItem
import com.dabenxiang.mimi.model.vo.PostVideoAttachment
import java.io.Serializable

data class PostClubItem(
    val type: Int = -1,
    val title: String = "",
    var request: String = "",
    val tags: ArrayList<String> = arrayListOf(),
    val uploadPics: ArrayList<PostAttachmentItem> = arrayListOf(),
    val deletePics: ArrayList<String> = arrayListOf(),
    val uploadVideo: ArrayList<PostVideoAttachment> = arrayListOf(),
    val deleteVideo: ArrayList<PostVideoAttachment> = arrayListOf(),
    val memberPostItem: MemberPostItem? = null
): Serializable