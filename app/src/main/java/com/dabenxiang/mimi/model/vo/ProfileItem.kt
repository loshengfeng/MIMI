package com.dabenxiang.mimi.model.vo

import java.util.*

data class ProfileItem(
    var userId: Long = 0,
    var deviceId: String = "",
    var account: String = "",
    var password: String = "",
    var avatarAttachmentId: Long = 0,
    var friendlyName: String = "",
    var point: Int = 0,
    var isEmailConfirmed: Boolean = false,
    var isSubscribed: Boolean = false,
    var expiryDate: Date = Date(),
    var videoCount: Int = 0,
    var videoOnDemandCount: Int = 0,
    var creationDate: Date = Date()
)
