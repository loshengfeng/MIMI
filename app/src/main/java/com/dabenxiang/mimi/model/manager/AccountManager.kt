package com.dabenxiang.mimi.model.manager

import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.manager.mqtt.MQTTManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.model.vo.ProfileItem
import com.dabenxiang.mimi.model.vo.TokenItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class AccountManager(
    private val pref: Pref,
    private val domainManager: DomainManager,
    private val mqttManager: MQTTManager
) {

    var keepAccount: Boolean
        get() = pref.keepAccount
        set(value) {
            pref.keepAccount = value
        }

    fun setupProfile(profileItem: ProfileItem) {
        pref.profileItem = profileItem
    }

    fun setupProfile(meItem: MeItem) {
        pref.profileItem = getProfile().copy(
            userId = meItem.id,
            avatarAttachmentId = meItem.avatarAttachmentId ?: 0,
            friendlyName = meItem.friendlyName ?: "",
            point = meItem.availablePoint ?: 0,
            isEmailConfirmed = meItem.isEmailConfirmed ?: false,
            videoCount = meItem.videoCount ?: 0,
            videoCountLimit = meItem.videoCountLimit ?: 0,
            videoOnDemandCount = meItem.videoOnDemandCount ?: 0,
            videoOnDemandCountLimit = meItem.videoOnDemandCountLimit ?: 0,
            isGuest = meItem.isGuest,
            isSubscribed = meItem.isSubscribed,
            expiryDate = meItem.expiryDate ?: Date(),
        )
    }

    fun getProfile(): ProfileItem {
        return pref.profileItem
    }

    fun hasMemberToken(): Boolean {
        val tokenItem = pref.memberToken
        return tokenItem.accessToken.isNotEmpty() && tokenItem.refreshToken.isNotEmpty()
    }

    fun isVip(): Boolean {
        return getProfile().isSubscribed
    }

    fun isLogin(): Boolean {
//        return when (getMemberTokenResult()) {
//            TokenResult.PASS -> true
//            TokenResult.EXPIRED -> getProfile().userId != 0L
//            else -> false
//        }
        return isVip() || isBindPhone()
    }

    fun isBindPhone(): Boolean {
        return !getProfile().isGuest
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

    fun bindPhone(request: BindPhoneRequest) =
        flow {
            val result = domainManager.getApiRepository().bindPhone(request)
            if (!result.isSuccessful) throw HttpException(result)
            Timber.i("signUpGuest bindPhone = ${result?.body()?.content}")
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e -> emit(ApiResult.error(e)) }
            .onCompletion { emit(ApiResult.loaded()) }

    fun signIn(userId:Long, userName: String? ="", code: String? = "") =
        flow {

            Timber.i("signUpGuest signIn userName:$userName   code:$code")
            val request = SingInRequest(
                id = userId,
                username = if(userName.isNullOrEmpty()) null else userName,
                code = if(code.isNullOrEmpty()) null else code
            )

            val result = domainManager.getApiRepository().signIn(request)
            if (!result.isSuccessful) throw HttpException(result)

            result.body()?.content?.also { item ->
                pref.memberToken = TokenItem(
                    accessToken = item.accessToken,
                    refreshToken = item.refreshToken,
                    expiresTimestamp = Date().time + (item.expiresIn - 120) * 1000
                )
            }
            Timber.i("doRegisterValidateAndSubmit friendlyName =${ pref.memberToken}")
            refreshUserInfo(userName)

            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .catch { e -> emit(ApiResult.error(e)) }

    private suspend fun refreshUserInfo(userName:String?){
        val meResult = domainManager.getApiRepository().getMe()
        if (!meResult.isSuccessful) throw HttpException(meResult)
        val meItem = meResult.body()?.content

        setupProfile(
            ProfileItem(
                userId = meItem?.id ?: 0,
                deviceId = GeneralUtils.getAndroidID(),
                account = userName ?: "",
                userName = userName ?: "",
                password = "",
                friendlyName = meItem?.friendlyName ?: "",
                avatarAttachmentId = meItem?.avatarAttachmentId ?: 0,
                isEmailConfirmed = meItem?.isEmailConfirmed ?: false,
                isSubscribed = meItem?.isSubscribed ?: false,
                expiryDate = meItem?.expiryDate ?: Date(),
                videoCount = meItem?.videoCount ?: 0,
                videoOnDemandCount = meItem?.videoOnDemandCount ?: 0,
                creationDate = meItem?.creationDate ?: Date(),
                isDailyCheckIn = meItem?.isDailyCheckIn ?: false,
                isGuest = meItem?.isGuest ?: true
            )
        )
    }

    fun signOut() =
        flow {
            val result = domainManager.getApiRepository().authSignOut()
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
        pref.clearSearchHistory()
        if (!keepAccount) pref.clearProfile()
        mqttManager.disconnect()
    }
}