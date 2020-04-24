package com.dabenxiang.mimi.model.manager

import android.text.TextUtils
import com.dabenxiang.mimi.model.api.ApiRepository
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ResetPasswordRequest
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.model.vo.ProfileData
import com.dabenxiang.mimi.model.vo.TokenData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import timber.log.Timber

class AccountManager(private val pref: Pref,
                     private val apiRepository: ApiRepository
) {

    fun getProfile(): ProfileData? {
        return pref.profileData
    }

    private fun setupProfile(profileData: ProfileData) {
        pref.profileData = profileData
    }

    private fun clearProfile() {
        pref.clearProfile()
    }

    fun isAutoLogin(): Boolean {
        return (!TextUtils.isEmpty(pref.token.accessToken)
                && !TextUtils.isEmpty(pref.token.refreshToken))
    }

    fun setupToken(tokenData: TokenData) {
        if (TextUtils.isEmpty(tokenData.refreshToken)) {
            pref.token = TokenData(tokenData.accessToken, pref.token.refreshToken)
        } else {
            pref.token = tokenData
        }
    }

    private fun clearToken() {
        pref.clearToken()
    }

    fun getToken() =
        flow {
            val result = apiRepository.getToken()
            if (!result.isSuccessful) throw HttpException(result)
            val tokenItem = result.body()
            Timber.d("Token: ${tokenItem?.accessToken}")
            if (tokenItem != null) {

                setupToken(TokenData(accessToken = tokenItem.accessToken))
            }
            emit(ApiResult.success(null))
        }
            .flowOn(Dispatchers.IO)
            .catch { e ->
                emit(ApiResult.error(e))
            }


//    fun login(userName: String, password: String) =
//        flow {
//            val request = LoginRequest(AppUtils.getAndroidID(), userName, password)
//            val result = apiRepository.login(request)
//            Timber.d("login result: $result")
//            if (!result.isSuccessful) throw HttpException(result)
//            val loginItem = result.body()?.data
//            setupToken(TokenData(loginItem!!.accessToken, loginItem.refreshToken))
//            setupProfile(ProfileData(AppUtils.getAndroidID(), userName, password))
//            emit(ApiResult.success(null))
//        }
//            .flowOn(Dispatchers.IO)
//            .onStart { emit(ApiResult.loading()) }
//            .catch { e ->
//                e.printStackTrace()
//                emit(ApiResult.error(e))
//            }.onCompletion {
//                emit(ApiResult.loaded())
//            }

//    fun logout() =
//        flow {
//            val result = apiRepository.logout()
//            if (!result.isSuccessful) throw HttpException(result)
//            socketManager.finish()
//            clearProfile()
//            clearToken()
//            emit(ApiResult.success(null))
//        }
//            .flowOn(Dispatchers.IO)
//            .onStart { emit(ApiResult.loading()) }
//            .catch { e ->
//                emit(ApiResult.error(e))
//            }.onCompletion {
//                emit(ApiResult.loaded())
//            }
//    fun refreshToken() =
//        flow {
//            val result = apiRepository.refreshToken(pref.token.refreshToken)
//            if (!result.isSuccessful) throw HttpException(result)
//            val refreshTokenItem = result.body()?.data
//            setupToken(TokenData(refreshTokenItem!!.accessToken, refreshTokenItem.refreshToken))
//            socketManager.startWebSocket()
//            emit(ApiResult.success(null))
//        }
//            .flowOn(Dispatchers.IO)
//            .catch { e ->
//                emit(ApiResult.error(e))
//            }

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
//        socketManager.finish()
        clearProfile()
        clearToken()
    }

}