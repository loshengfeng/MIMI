package com.dabenxiang.mimi.model.pref

import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.model.vo.SearchHistoryItem
import com.dabenxiang.mimi.model.vo.TokenItem
import com.google.gson.Gson

class Pref(private val gson: Gson, preferenceFileName: String, isDebug: Boolean) :
    AbstractPref(preferenceFileName, isDebug) {

    private val tokenPref = StringPref("TOKEN")
    private val memberTokenPref = StringPref("MEMBER_TOKEN")
    private val profilePref = StringPref("PROFILE")
    private val keepAccountPref = BooleanPref("KEEP_ACCOUNT")
    private var searchHistoryPref = StringPref("SEARCH_HISTORY")

    private var cachedPublicToken: TokenItem? = null
    private var cachedMemberToken: TokenItem? = null

    var publicToken: TokenItem
        get() =
            try {
                if (cachedPublicToken == null) {
                    cachedPublicToken = gson.fromJson(tokenPref.get(), TokenItem::class.java)
                }

                cachedPublicToken ?: TokenItem()
            } catch (e: Exception) {
                TokenItem()
            }
        set(value) {
            cachedPublicToken = value
            tokenPref.set(gson.toJson(value))
        }

    var memberToken: TokenItem
        get() =
            try {
                if (cachedMemberToken == null) {
                    cachedMemberToken = gson.fromJson(memberTokenPref.get(), TokenItem::class.java)
                }

                cachedMemberToken ?: TokenItem()
            } catch (e: Exception) {
                TokenItem()
            }
        set(value) {
            cachedMemberToken = value
            memberTokenPref.set(gson.toJson(value))
        }

    var profileItem: ProfileItem
        get() =
            try {
                gson.fromJson(profilePref.get(), ProfileItem::class.java)
            } catch (e: Exception) {
                ProfileItem()
            }
        set(value) {
            profilePref.set(gson.toJson(value))
        }

    var searchHistoryItem: SearchHistoryItem
        get() =
            try {
                gson.fromJson(searchHistoryPref.get(), SearchHistoryItem::class.java)
            } catch (e: Exception) {
                SearchHistoryItem()
            }
        set(value) {
            searchHistoryPref.set(gson.toJson(value))
        }

    var keepAccount: Boolean
        get() = keepAccountPref.get()
        set(value) = keepAccountPref.set(value)

    fun clearMemberToken() {
        cachedMemberToken = null
        memberTokenPref.remove()
    }

    fun clearProfile() {
        profilePref.remove()
    }
}
