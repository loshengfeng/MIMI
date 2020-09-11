package com.dabenxiang.mimi.model.vo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostVideoAttachment (
    var picAttachmentId: String = "",
    var videoAttachmentId: String = "",
    var picUrl: String = "",
    var videoUrl: String = "",
    var ext: String = "",
    var length: String = ""
) : Parcelable