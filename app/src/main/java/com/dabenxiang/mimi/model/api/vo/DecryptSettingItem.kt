package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.model.enums.CipherMode
import com.google.gson.annotations.SerializedName

data class DecryptSettingItem(
    @SerializedName("id")
    val id: Long? = 0,

    @SerializedName("source")
    val source: String? = "",

    @SerializedName("key")
    val key: ByteArray? = null,

    @SerializedName("iv")
    val iv: ByteArray? = null,

    @SerializedName("cipherMode")
    val cipherMode: CipherMode = CipherMode.CBC,

    @SerializedName("isImageDecrypt")
    val isImageDecrypt: Boolean = false,

    @SerializedName("isVideoDecrypt")
    val isVideoDecrypt: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecryptSettingItem

        if (id != other.id) return false
        if (source != other.source) return false
        if (key != null) {
            if (other.key == null) return false
            if (!key.contentEquals(other.key)) return false
        } else if (other.key != null) return false
        if (iv != null) {
            if (other.iv == null) return false
            if (!iv.contentEquals(other.iv)) return false
        } else if (other.iv != null) return false
        if (cipherMode != other.cipherMode) return false
        if (isImageDecrypt != other.isImageDecrypt) return false
        if (isVideoDecrypt != other.isVideoDecrypt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (source?.hashCode() ?: 0)
        result = 31 * result + (key?.contentHashCode() ?: 0)
        result = 31 * result + (iv?.contentHashCode() ?: 0)
        result = 31 * result + cipherMode.hashCode()
        result = 31 * result + isImageDecrypt.hashCode()
        result = 31 * result + isVideoDecrypt.hashCode()
        return result
    }
}