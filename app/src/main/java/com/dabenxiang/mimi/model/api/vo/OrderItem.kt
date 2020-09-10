package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.OrderStatus
import com.dabenxiang.mimi.model.enums.OrderType
import com.dabenxiang.mimi.model.enums.PaymentStatus
import com.dabenxiang.mimi.model.enums.PaymentType
import com.google.gson.annotations.SerializedName
import java.util.*

data class OrderItem(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("chatId")
    val chatId: Long = 0,

    @SerializedName("userFriendlyName")
    val userFriendlyName: String = "",

    @SerializedName("packageName")
    val packageName: String = "",

    @SerializedName("packageListPrice")
    val packageListPrice: Float = 0f,

    @SerializedName("packagePrice")
    val packagePrice: Float = 0f,

    @SerializedName("packagePoint")
    val packagePoint: Int = 0,

    @SerializedName("merchantUserFriendlyName")
    val merchantUserFriendlyName: String = "",

    @SerializedName("merchantUserAvatarAttachmentId")
    val merchantUserAvatarAttachmentId: Long? = 0,

    @SerializedName("type")
    val type: OrderType = OrderType.USER2ONLINE,

    @SerializedName("paymentType")
    val paymentType: PaymentType = PaymentType.BANK,

    @SerializedName("paymentStatus")
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,

    @SerializedName("sellingPrice")
    val sellingPrice: Float = 0f,

    @SerializedName("status")
    val status: OrderStatus = OrderStatus.PENDING,

    @SerializedName("createTime")
    val createTime: Date? = null,

    @SerializedName("completionTime")
    val completionTime: Date? = null,

    @SerializedName("accountName")
    val accountName: String = "",

    @SerializedName("accountNumber")
    val accountNumber: String = "",

    @SerializedName("isOnline")
    val isOnline: Boolean = false,

    @SerializedName("paymentInfos")
    val paymentInfos: ArrayList<PaymentInfoItem> = arrayListOf(),

    @SerializedName("traceLogId")
    val traceLogId: Long = 0,

    @SerializedName("lastReplyTime")
    val lastReplyTime: Date? = null,

    @SerializedName("lastReadTime")
    val lastReadTime: Date? = null,

    @SerializedName("actualAmount")
    val actualAmount: Float = 0f,

    @SerializedName("point")
    val point: Int = 0,

    @SerializedName("failureReason")
    val failureReason: String = ""
)