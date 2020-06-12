package com.dabenxiang.mimi.model.vo

data class ProfileData(
    var deviceId: String = "",
    var account: String = "",
    var password: String = "",
    var avatarAttachmentId : Long = 0,
    var friendlyName: String = ""
)
