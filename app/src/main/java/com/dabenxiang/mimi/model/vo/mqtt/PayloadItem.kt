package com.dabenxiang.mimi.model.vo.mqtt

import com.dabenxiang.mimi.model.enums.NotifyType
import com.google.gson.annotations.SerializedName

open class PayloadItem(

    @SerializedName("type")
    val type: NotifyType = NotifyType.CREATE_ORDER,
)