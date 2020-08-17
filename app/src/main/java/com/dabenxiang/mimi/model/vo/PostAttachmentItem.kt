package com.dabenxiang.mimi.model.vo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostAttachmentItem (
    var attachmentId: String = "",
    var uri: String = "",
    var ext: String = ""
) : Parcelable