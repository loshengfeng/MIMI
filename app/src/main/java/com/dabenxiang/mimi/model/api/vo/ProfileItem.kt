package com.dabenxiang.mimi.model.api.vo

import com.dabenxiang.mimi.R
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ProfileItem : Serializable{
    /**
     * username : kevinlove
     * friendlyName : 1234567890
     * email : jeff.yu@silkrode.com
     * gender : 0
     * avatarAttachmentId : 3811453500018130944
     * emailConfirmed : false
     */
    @SerializedName("username")
    var username: String? = null

    @SerializedName("friendlyName")
    var friendlyName: String? = null

    @SerializedName("email")
    var email: String? = null

    // 0:Female|1:Male default is Female
    @SerializedName("gender")
    var gender: Int? = null

    @SerializedName("birthday")
    var birthday: String? = null

    @SerializedName("avatarAttachmentId")
    var avatarAttachmentId: Long = 0

    @SerializedName("emailConfirmed")
    var emailConfirmed = false

    fun getGenderRes(): Int {
        return when(gender) {
            0 -> R.string.text_gender_female
            1 -> R.string.text_gender_male
            else -> R.string.setting_choose
        }
    }
}



