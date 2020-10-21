package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.R
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PromotionItem : Serializable{
    @SerializedName("code")
    var promotion_code: String? = null

    @SerializedName("url")
    var promotion_url: String? = null

    @SerializedName("promotionNumber")
    var promotionNumber: Int = 0

    @SerializedName("cumulativeDays")
    var cumulativeDays: Int = 0
}



