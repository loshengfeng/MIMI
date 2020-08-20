package com.dabenxiang.mimi.model.manager

import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChangePasswordRequest
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.model.api.vo.SignInRequest
import com.dabenxiang.mimi.model.api.vo.SingUpRequest
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.model.vo.TokenItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.util.*

class AccountManager(private val pref: Pref, private val domainManager: DomainManager) {

    var keepAccount: Boolean
        get() = pref.keepAccount
        set(value) {
            pref.keepAccount = value
        }

    private fun setupProfile(profileItem: ProfileItem) {
        pref.profileItem = profileItem
    }

    fun setupProfile(meItem: MeItem) {
        pref.profileItem.userId = meItem.id ?: 0
        pref.profileItem.avatarAttachmentId = meItem.avatarAttachmentId ?: 0
        pref.profileItem.friendlyName = meItem.friendlyName ?: ""
        pref.profileItem.point = meItem.availablePoint ?: 0
        pref.profileItem.isEmailConfirmed = meItem.isEmailConfirmed ?: false
    }

    fun getProfile(): ProfileItem {
        return pref.profileItem
    }

    fun setupMeAvatarCache(avatar: ByteArray?) {
        avatar?.let { pref.meAvatar = it }
    }

    fun getMeAvatarCache(): ByteArray? {
        return pref.meAvatar
    }

    fun hasMemberToken(): Boolean {
        val tokenItem = pref.memberToken
        return tokenItem.accessToken.isNotEmpty() && tokenItem.refreshToken.isNotEmpty()
    }

    fun isLogin(): Boolean {
        return getMemberTokenResult() == TokenResult.PASS
    }

    fun getMemberTokenResult(): TokenResult {
        val tokenData = pref.memberToken
        return when {
            tokenData.expiresTimestamp == 0L -> TokenResult.EMPTY
            tokenData.accessToken.isEmpty() -> TokenResult.EMPTY
            tokenData.refreshToken.isEmpty() -> TokenResult.EMPTY
            Date().time > tokenData.expiresTimestamp -> TokenResult.EXPIRED
            else -> TokenResult.PASS
        }
    }

    fun getPublicTokenResult(): TokenResult {
        val tokenData = pref.publicToken
        return when {
            tokenData.expiresTimestamp == 0L -> TokenResult.EMPTY
            tokenData.accessToken.isEmpty() -> TokenResult.EMPTY
            Date().time > tokenData.expiresTimestamp -> TokenResult.EMPTY
            else -> TokenResult.PASS
        }
    }

    fun getPublicToken() =
        flow {
            val result = domainManager.getApiRepository().getToken()
            if (!result.isSuccessful) throw HttpException(result)
            result.body()?.also { tokenItem ->
                pref.publicToken =
                    TokenItem(
                        accessToken = tokenItem.accessToken,
                        expiresTimestamp = Date().time + (tokenItem.expiresIn - 120) * 1000 // 提前2分鐘過期
                    )
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .catch { e -> emit(ApiResult.error(e)) }

    fun refreshToken() =
        flow {
            val result =
                domainManager.getApiRepository().refreshToken(pref.memberToken.refreshToken)
            if (!result.isSuccessful) throw HttpException(result)
            result.body()?.also { item ->
                pref.memberToken = TokenItem(
                    accessToken = item.accessToken,
                    refreshToken = item.refreshToken,
                    expiresTimestamp = Date().time + (item.expiresIn - 120) * 1000  // 提前2分鐘過期
                )
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .catch { e -> emit(ApiResult.error(e)) }

    fun signUp(request: SingUpRequest) =
        flow {
            val result = domainManager.getApiRepository().signUp(request)
            if (!result.isSuccessful) throw HttpException(result)
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e -> emit(ApiResult.error(e)) }
            .onCompletion { emit(ApiResult.loaded()) }

    fun signIn(userName: String, password: String) =
        flow {
            val request = SignInRequest(userName, password)
            val result = domainManager.getApiRepository().signIn(request)
            if (!result.isSuccessful) throw HttpException(result)

            result.body()?.content?.also { item ->
                pref.memberToken = TokenItem(
                    accessToken = item.accessToken,
                    refreshToken = item.refreshToken,
                    // 提前2分鐘過期
                    expiresTimestamp = Date().time + (item.expiresIn - 120) * 1000
                )
            }

            if (getProfile().userId == 0L) {
                val meResult = domainManager.getApiRepository().getMe()
                if (!meResult.isSuccessful) throw HttpException(meResult)

                val meItem = meResult.body()?.content
                setupProfile(
                    ProfileItem(
                        userId = meItem?.id ?: 0,
                        deviceId = GeneralUtils.getAndroidID(),
                        account = userName,
                        password = password,
                        friendlyName = meItem?.friendlyName ?: "",
                        avatarAttachmentId = meItem?.avatarAttachmentId ?: 0,
                        isEmailConfirmed = meItem?.isEmailConfirmed ?: false
                    )
                )
            }

            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e -> emit(ApiResult.error(e)) }
            .onCompletion { emit(ApiResult.loaded()) }

    fun signOut() =
        flow {
            val result = domainManager.getApiRepository().signOut()
            if (!result.isSuccessful) throw HttpException(result)

            logoutLocal()

            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e -> emit(ApiResult.error(e)) }
            .onCompletion { emit(ApiResult.loaded()) }


    fun changePwd(oldPassword: String, newPassword: String) =
        flow {
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val result = domainManager.getApiRepository().changePassword(request)
            if (!result.isSuccessful) throw HttpException(result)
            setupProfile(getProfile().copy(password = newPassword))
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e -> emit(ApiResult.error(e)) }
            .onCompletion { emit(ApiResult.loaded()) }

    fun logoutLocal() {
        pref.clearMemberToken()
        pref.clearProfile()
    }
}