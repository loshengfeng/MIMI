package com.dabenxiang.mimi.model.vo.domain

import com.google.gson.annotations.SerializedName

data class DomainOutputListItem(

    @SerializedName("version")
    val version: String = "",

    @SerializedName("data")
    val domainOutputs: List<DomainOutputItem> = arrayListOf()
)
