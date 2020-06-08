package com.dabenxiang.mimi.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.LoginRequest
import com.dabenxiang.mimi.model.api.vo.ResetPasswordRequest
import com.dabenxiang.mimi.model.enums.TokenResult
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.model.vo.ProfileData
import com.dabenxiang.mimi.model.vo.TokenData
import com.dabenxiang.mimi.widget.utility.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.util.*

class AccountManager(private val pref: Pref, private val apiRepository: ApiRepository) {
    private val _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean> = _isLogin

    fun getProfile(): ProfileData {
        return pref.profileData
    }

    var keepAccount: Boolean
        get() = pref.keepAccount
        set(value) {
            pref.keepAccount = value
        }

    private fun setupProfile(profileData: ProfileData) {
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
            val result = apiRepository.getToken()
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
            .catch { e ->
                emit(ApiResult.error(e))
            }


    fun refreshToken() =
        flow {
            val result = apiRepository.refreshToken(pref.memberToken.refreshToken)
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
            .catch { e ->
                emit(ApiResult.error(e))
            }


    // todo: 04/06/2020
    fun register(userName: String, password: String) =
        flow {
            val request = LoginRequest(userName, password)
            val result = apiRepository.signIn(request)
            if (!result.isSuccessful) throw HttpException(result)

            result.body()?.content?.also { item ->
                pref.memberToken = TokenData(
                    accessToken = item.accessToken,
                    refreshToken = item.refreshToken,
                    // 提前2分鐘過期
                    expiresTimestamp = Date().time + (item.expiresIn - 120) * 1000
                )
            }

            setupProfile(ProfileData(AppUtils.getAndroidID(), userName, password))

            _isLogin.postValue(true)

            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e ->
                emit(ApiResult.error(e))
            }.onCompletion {
                emit(ApiResult.loaded())
            }

    fun signIn(userName: String, password: String) =
        flow {
            val request = LoginRequest(userName, password)
            val result = apiRepository.signIn(request)
            if (!result.isSuccessful) throw HttpException(result)

            result.body()?.content?.also { item ->
                pref.memberToken = TokenData(
                    accessToken = item.accessToken,
                    refreshToken = item.refreshToken,
                    // 提前2分鐘過期
                    expiresTimestamp = Date().time + (item.expiresIn - 120) * 1000
                )
            }

            setupProfile(ProfileData(AppUtils.getAndroidID(), userName, password))

            _isLogin.postValue(true)

            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e ->
                emit(ApiResult.error(e))
            }.onCompletion {
                emit(ApiResult.loaded())
            }

    fun signOut() =
        flow {
            val result = apiRepository.signOut()
            if (!result.isSuccessful) throw HttpException(result)

            logoutLocal()

            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e ->
                emit(ApiResult.error(e))
            }.onCompletion {
                emit(ApiResult.loaded())
            }

    fun resetPwd(userName: String, newPwd: String) =
        flow {
            val request = ResetPasswordRequest(userName, newPwd)
            val result = apiRepository.resetPassword(request)
            if (!result.isSuccessful) throw HttpException(result)
            getProfile()?.copy(password = newPwd)?.let {
                setupProfile(it)
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .onStart { emit(ApiResult.loading()) }
            .catch { e ->
                emit(ApiResult.error(e))
            }.onCompletion {
                emit(ApiResult.loaded())
            }

    fun logoutLocal() {
        pref.clearMemberToken()
        _isLogin.postValue(false)
    }
}