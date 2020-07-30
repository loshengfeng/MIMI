package com.dabenxiang.mimi.manager.update.data

import com.google.gson.annotations.SerializedName

data class UpdateBaseItem<T>(

    @SerializedName("content")
    val data: T
)
