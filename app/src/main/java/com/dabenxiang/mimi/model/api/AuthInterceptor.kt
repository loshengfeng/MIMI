package com.dabenxiang.mimi.model.api

import com.dabenxiang.mimi.model.manager.AccountManager
import com.dabenxiang.mimi.model.pref.Pref
import com.dabenxiang.mimi.model.vo.TokenData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.net.HttpURLConnection

class AuthInterceptor(private val pref: Pref) : Interceptor, KoinComponent {
    private val accountManager: AccountManager by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenData = pref.token
        Timber.d("Token: ${tokenData.accessToken}")
        return if (tokenData.accessToken.isBlank()) {
            runBlocking {
                withContext(Dispatchers.IO) {
                    accountManager.getToken().collect {
                        when (it) {
                            is ApiResult.Empty -> Timber.d("Get token successful!")
                            is ApiResult.Error -> Timber.e("Get token error: $it")
                        }
                    }
                    Timber.d("Token: ${tokenData.accessToken}")
                    chain.proceed(chain.buildRequest(pref.token))
                }
            }
        } else {
            val response = chain.proceed(chain.buildRequest(tokenData))
            return return when (response.code) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            // TODO: Refresh token
                        }
                        chain.proceed(chain.buildRequest(pref.token))
                    }
                }
                else -> response
            }
        }
    }

    private fun Interceptor.Chain.buildRequest(tokenData: TokenData): Request {
        val requestBuilder = request().newBuilder()
        requestBuilder.addHeader("Authorization", "Bearer ${tokenData.accessToken}")
        return requestBuilder.build()
    }
}
