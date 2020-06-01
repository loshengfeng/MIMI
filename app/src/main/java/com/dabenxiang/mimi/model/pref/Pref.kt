package com.dabenxiang.mimi.model.pref

import com.dabenxiang.mimi.model.vo.ProfileData
import com.dabenxiang.mimi.model.vo.TokenData
import com.google.gson.Gson

class Pref(private val gson: Gson, preferenceFileName: String, isDebug: Boolean) : AbstractPref(preferenceFileName, isDebug) {

    private val tokenPref = StringPref("TOKEN")
    private val memberTokenPref = StringPref("MEMBER_TOKEN")
    private val aesKeyPref = StringPref("AES_KEY")
    private val ellipsizeKeyPref = BooleanPref("ELLIPSIZE_KEY")
    private val profilePref = StringPref("PROFILE")
    private var cachedPublicToken: TokenData? = null
    private var cachedMemberToken: TokenData? = null

    var publicToken: TokenData
        get() =
            try {
                if (cachedPublicToken == null) {
                    cachedPublicToken = gson.fromJson(tokenPref.get(), TokenData::class.java)
                }

                cachedPublicToken ?: TokenData()
            } catch (e: Exception) {
                TokenData()
            }
        set(value) {
            cachedPublicToken = value
            tokenPref.set(gson.toJson(value))
        }

    var memberToken: TokenData
        get() =
            try {
                if (cachedMemberToken == null) {
                    cachedMemberToken = gson.fromJson(memberTokenPref.get(), TokenData::class.java)
                }

                cachedMemberToken ?: TokenData()
            } catch (e: Exception) {
                TokenData()
            }
        set(value) {
            cachedMemberToken = value
            memberTokenPref.set(gson.toJson(value))
        }

    var profileData: ProfileData
        get() =
            try {
                gson.fromJson(profilePref.get(), ProfileData::class.java)
            } catch (e: Exception) {
                ProfileData()
            }
        set(value) {
            profilePref.set(gson.toJson(value))
        }

    var aesKey: String
        get() = aesKeyPref.get().toString()
        set(value) = aesKeyPref.set(value)

    var disableEllipsize: Boolean
        get() = ellipsizeKeyPref.get()
        set(value) = ellipsizeKeyPref.set(value)

    fun clearToken() {
        tokenPref.remove()
    }

    fun clearProfile() {
        profilePref.remove()
    }
}
