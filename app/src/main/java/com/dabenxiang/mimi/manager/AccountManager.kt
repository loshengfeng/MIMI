package com.dabenxiang.mimi.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.*
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.model.vo.ProfileData
import com.dabenxiang.mimi.model.vo.TokenData
import com.dabenxiang.mimi.widget.utility.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.util.*

class AccountManager(private val pref: Pref, private val domainManager: DomainManager) {
    private val _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean> = _isLogin

    fun getProfile(): ProfileData { return pref.profileData }

    var keepAccount: Boolean
        get() = pref.keepAccount
        set(value) {
            pref.keepAccount = value
        }

    fun setupProfile(profileData: ProfileData) {
        pref.profileData = profileData
    }

    fun hasMemberToken(): Boolean {
        val tokenItem = pref.memberToken
        return tokenItem.accessToken.isNotEmpty() && tokenItem.refreshToken.isNotEmpty()
    }

    fun isAutoLogin(): Boolean {
        val tokenItem = pref.memberToken
        return tokenItem.accessToken.isNotEmpty() && tokenItem.refreshToken.isNotEmpty()
    }

    fun getMemberTokenResult(): TokenResult {
        val tokenData = pref.memberToken
        return when {
            tokenData.expiresTimestamp == 0L -> TokenResult.Empty
            tokenData.accessToken.isEmpty() -> TokenResult.Empty
            tokenData.refreshToken.isEmpty() -> TokenResult.Empty
            Date().time > tokenData.expiresTimestamp -> TokenResult.Expired
            else -> TokenResult.Pass
        }
    }

    fun getPublicTokenResult(): TokenResult {
        val tokenData = pref.publicToken
        return when {
            tokenData.expiresTimestamp == 0L -> TokenResult.Empty
            tokenData.accessToken.isEmpty() -> TokenResult.Empty
            Date().time > tokenData.expiresTimestamp -> TokenResult.Expired
            else -> TokenResult.Pass
        }
    }

    fun getPublicToken() =
        flow {
            val result = domainManager.getApiRepository().getToken()
            if (!result.isSuccessful) throw HttpException(result)
            result.body()?.also { tokenItem ->
                pref.publicToken =
                    TokenData(
                        accessToken = tokenItem.accessToken,
                        // 提前2分鐘過期
                        expiresTimestamp = Date().time + (tokenItem.expiresIn - 120) * 1000
                    )
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .catch { e -> emit(ApiResult.error(e)) }

    fun refreshToken() =
        flow {
            val result = domainManager.getApiRepository().refreshToken(pref.memberToken.refreshToken)
            if (!result.isSuccessful) throw HttpException(result)
            result.body()?.also { item ->
                pref.memberToken = TokenData(
                    accessToken = item.accessToken,
                    refreshToken = item.refreshToken,
                    // 提前2分鐘過期
                    expiresTimestamp = Date().time + (item.expiresIn - 120) * 1000
                )
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .catch { e -> emit(ApiResult.error(e)) }

    fun singUp(request: SingUpRequest) =
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
                pref.memberToken = TokenData(
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
                setupProfile(ProfileData(userId = meItem?.id ?: 0, deviceId = AppUtils.getAndroidID(), account = userName, password = password, friendlyName = meItem?.friendlyName ?: ""))
            }

            _isLogin.postValue(true)

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
            getProfile()?.copy(password = newPassword)?.let {
                setupProfile(it)
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e -> emit(ApiResult.error(e)) }
            .onCompletion { emit(ApiResult.loaded()) }

    fun logoutLocal() {
        pref.clearMemberToken()
        pref.clearProfile()
        _isLogin.postValue(false)
    }
}